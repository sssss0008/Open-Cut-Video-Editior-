package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }
    val currentUser = auth?.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class CloudProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val aspectRatio: String = "16:9",
    val durationMs: Long = 0L,
    val clipCount: Long = 0L,
    val activeFilterPreset: String = "Original",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CreatorAnalytics(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val totalProjectsCount: Long = 0L,
    val exportedVideosCount: Long = 0L,
    val totalTimelineDurationMs: Long = 0L,
    val favoriteFilterPreset: String = "Original",
    val cloudStorageUsedKb: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class CloudSyncRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeCloudProjects(userId: String): Flow<List<CloudProject>> {
        return db.collection("users")
            .document(userId)
            .collection("projects")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CloudProject::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            }
    }

    fun observeCreatorAnalytics(userId: String): Flow<CreatorAnalytics?> {
        return db.collection("users")
            .document(userId)
            .collection("analytics")
            .document("summary")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        CreatorAnalytics::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
    }

    suspend fun syncProjectToCloud(
        userId: String,
        projectId: String,
        title: String,
        aspectRatio: String,
        durationMs: Long,
        clipCount: Long,
        activeFilterPreset: String
    ): Result<String> = runCatching {
        val safeId = projectId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifBlank { "proj_default" }
        val docRef = db.collection("users")
            .document(userId)
            .collection("projects")
            .document(safeId)

        val safeTitle = title.trim().ifBlank { "Untitled Project" }.take(100)
        val safeRatio = aspectRatio.trim().ifBlank { "16:9" }.take(20)
        val safePreset = activeFilterPreset.trim().ifBlank { "Original" }.take(60)
        val safeDuration = durationMs.coerceIn(0L, 86400000L)
        val safeClips = clipCount.coerceIn(0L, 10000L)

        try {
            val createPayload = mapOf(
                "userId" to userId,
                "title" to safeTitle,
                "aspectRatio" to safeRatio,
                "durationMs" to safeDuration,
                "clipCount" to safeClips,
                "activeFilterPreset" to safePreset,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(createPayload).await()
            safeId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun getCloudProjects(userId: String): Result<List<CloudProject>> = runCatching {
        val collectionRef = db.collection("users").document(userId).collection("projects")
        try {
            val snapshot = collectionRef.whereEqualTo("userId", userId).get().await()
            snapshot.toObjects(
                CloudProject::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, collectionRef.path)
            throw e
        }
    }

    suspend fun getCloudProjectById(userId: String, projectId: String): Result<CloudProject?> = runCatching {
        val docRef = db.collection("users").document(userId).collection("projects").document(projectId)
        try {
            val snapshot = docRef.get().await()
            snapshot.toObject(
                CloudProject::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun deleteCloudProject(userId: String, projectId: String): Result<Unit> = runCatching {
        val docRef = db.collection("users").document(userId).collection("projects").document(projectId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun syncAnalyticsSummary(
        userId: String,
        displayName: String,
        totalProjectsCount: Long,
        exportedVideosCount: Long,
        totalTimelineDurationMs: Long,
        favoriteFilterPreset: String,
        cloudStorageUsedKb: Long,
        summaryDocId: String = "summary"
    ): Result<Unit> = runCatching {
        val safeDocId = summaryDocId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifBlank { "summary" }
        val docRef = db.collection("users")
            .document(userId)
            .collection("analytics")
            .document(safeDocId)

        val safeName = displayName.trim().ifBlank { "OpenCut Creator" }.take(80)
        val safeProjects = totalProjectsCount.coerceIn(0L, 100000L)
        val safeExports = exportedVideosCount.coerceIn(0L, 100000L)
        val safeDuration = totalTimelineDurationMs.coerceIn(0L, 31536000000L)
        val safePreset = favoriteFilterPreset.trim().ifBlank { "Original" }.take(60)
        val safeStorage = cloudStorageUsedKb.coerceIn(0L, 1073741824L)

        try {
            val createPayload = mapOf(
                "userId" to userId,
                "displayName" to safeName,
                "totalProjectsCount" to safeProjects,
                "exportedVideosCount" to safeExports,
                "totalTimelineDurationMs" to safeDuration,
                "favoriteFilterPreset" to safePreset,
                "cloudStorageUsedKb" to safeStorage,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(createPayload).await()
            } catch (_: FirebaseFirestoreException) {
                val updatePayload = mapOf(
                    "displayName" to safeName,
                    "totalProjectsCount" to safeProjects,
                    "exportedVideosCount" to safeExports,
                    "totalTimelineDurationMs" to safeDuration,
                    "favoriteFilterPreset" to safePreset,
                    "cloudStorageUsedKb" to safeStorage,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
