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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: ImportedMediaEntity)

    @Delete
    suspend fun deleteMedia(media: ImportedMediaEntity)

    @Query("DELETE FROM imported_media WHERE id = :id")
    suspend fun deleteMediaById(id: String)
}
