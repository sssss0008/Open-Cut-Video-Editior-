package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedMediaDao {
    @Query("SELECT * FROM imported_media ORDER BY addedAt DESC")
    fun getAllMedia(): Flow<List<ImportedMediaEntity>>

    @Query("SELECT * FROM imported_media WHERE mediaType = :type ORDER BY addedAt DESC")
    fun getMediaByType(type: String): Flow<List<ImportedMediaEntity>>

    @Query("SELECT * FROM imported_media WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): ImportedMediaEntity?

    @Query("SELECT * FROM imported_media WHERE (name = :name AND sizeBytes = :sizeBytes AND sizeBytes > 0) OR uri = :uri LIMIT 1")
    suspend fun findDuplicate(name: String, sizeBytes: Long, uri: String): ImportedMediaEntity?

    @Query("UPDATE imported_media SET isPlaybackVerified = :verified WHERE id = :id")
    suspend fun setPlaybackVerified(id: String, verified: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: ImportedMediaEntity)

    @Delete
    suspend fun deleteMedia(media: ImportedMediaEntity)

    @Query("DELETE FROM imported_media WHERE id = :id")
    suspend fun deleteMediaById(id: String)
}
