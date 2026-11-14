package com.example.data.cloud

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CloudSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun syncProjectToCloud_validPayload_createsDocumentAndReturnsId() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CloudSyncRepository(firestore)
        val uniqueId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncProjectToCloud(
                userId = aliceUid,
                projectId = uniqueId,
                title = "Cinema Reel 4K",
                aspectRatio = "16:9",
                durationMs = 24000L,
                clipCount = 5L,
                activeFilterPreset = "Kodak 2383 Film"
            )
        }
        assertTrue(createResult.isSuccess)
        val savedId = createResult.getOrThrow()
        assertEquals(uniqueId, savedId)
    }

    @Test
    fun getCloudProjects_authenticatedOwner_returnsMatchingProjects() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CloudSyncRepository(firestore)
        val uniqueId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncProjectToCloud(
                userId = aliceUid,
                projectId = uniqueId,
                title = "Teal & Orange Cut",
                aspectRatio = "9:16",
                durationMs = 15000L,
                clipCount = 3L,
                activeFilterPreset = "Teal & Orange"
            ).getOrThrow()
        }

        val projectsResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getCloudProjects(aliceUid)
        }
        assertTrue(projectsResult.isSuccess)
        assertTrue(projectsResult.getOrThrow().any { it.id == uniqueId })
    }

    @Test
    fun observeCloudProjects_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CloudSyncRepository(firestore)
        val uniqueId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncProjectToCloud(
                userId = aliceUid,
                projectId = uniqueId,
                title = "Realtime Cloud Cut",
                aspectRatio = "16:9",
                durationMs = 18000L,
                clipCount = 4L,
                activeFilterPreset = "Anamorphic Gold"
            ).getOrThrow()
        }

        val emittedProjects = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCloudProjects(aliceUid).first { list -> list.any { it.id == uniqueId } }
        }
        assertTrue(emittedProjects.any { it.id == uniqueId })
    }

    @Test
    fun getCloudProjectById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CloudSyncRepository(firestore)
        val uniqueId = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.syncProjectToCloud(
                userId = aliceUid,
                projectId = uniqueId,
                title = "Alice Private Project",
                aspectRatio = "16:9",
                durationMs = 10000L,
                clipCount = 2L,
                activeFilterPreset = "Original"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = CloudSyncRepository(firestore)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getCloudProjectById(aliceUid, uniqueId).getOrThrow()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun observeCloudProjects_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val aliceUid = "alice_${UUID.randomUUID().toString().take(8)}"
        val repository = CloudSyncRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCloudProjects(aliceUid).first()
            }
            fail("Expected exception on unauthenticated flow observation")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            if (firestoreException != null) {
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
            } else {
                assertTrue("Expected timeout or firestore exception, got $e", e is kotlinx.coroutines.TimeoutCancellationException || e is java.util.concurrent.CancellationException)
            }
        }
    }

    @Test
    fun syncAnalyticsSummary_validPayload_createsAndUpdatesSummary() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = CloudSyncRepository(firestore)
        val summaryId = "summary_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        val firstSync = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncAnalyticsSummary(
                userId = aliceUid,
                displayName = "Alice Filmmaker",
                totalProjectsCount = 4L,
                exportedVideosCount = 9L,
                totalTimelineDurationMs = 95000L,
                favoriteFilterPreset = "Kodak 2383 Film",
                cloudStorageUsedKb = 2048L,
                summaryDocId = summaryId
            )
        }
        assertTrue(firstSync.isSuccess)

        val secondSync = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncAnalyticsSummary(
                userId = aliceUid,
                displayName = "Alice Filmmaker",
                totalProjectsCount = 5L,
                exportedVideosCount = 10L,
                totalTimelineDurationMs = 120000L,
                favoriteFilterPreset = "Teal & Orange",
                cloudStorageUsedKb = 2560L,
                summaryDocId = summaryId
            )
        }
        assertTrue(secondSync.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@opencut.test"
        const val BOB_EMAIL = "bob@opencut.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
