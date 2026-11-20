package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExportPresetDao {
    @Query("SELECT * FROM export_presets ORDER BY isBuiltIn ASC, updatedAt DESC")
    fun getAllPresets(): Flow<List<ExportPresetEntity>>

    @Query("SELECT * FROM export_presets ORDER BY isBuiltIn ASC, updatedAt DESC")
    suspend fun getAllPresetsSync(): List<ExportPresetEntity>

    @Query("SELECT * FROM export_presets WHERE platformName = :platformName ORDER BY isBuiltIn ASC, updatedAt DESC")
    fun getPresetsByPlatform(platformName: String): Flow<List<ExportPresetEntity>>

    @Query("SELECT * FROM export_presets WHERE isBuiltIn = 0 ORDER BY updatedAt DESC")
    fun getCustomPresets(): Flow<List<ExportPresetEntity>>

    @Query("SELECT * FROM export_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: String): ExportPresetEntity?

    @Query("SELECT * FROM export_presets WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getPresetByName(name: String): ExportPresetEntity?

    @Query("SELECT COUNT(*) FROM export_presets")
    suspend fun getPresetCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: ExportPresetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<ExportPresetEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPresetsIfMissing(presets: List<ExportPresetEntity>)

    @Update
    suspend fun updatePreset(preset: ExportPresetEntity)

    @Query("DELETE FROM export_presets WHERE id = :id")
    suspend fun deletePresetById(id: String)
}
