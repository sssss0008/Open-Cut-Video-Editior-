package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectLayerDao {
    @Query("SELECT * FROM project_layers WHERE projectId = :projectId ORDER BY layerIndex ASC")
    fun getLayersForProject(projectId: String): Flow<List<ProjectLayerEntity>>

    @Query("SELECT * FROM project_layers WHERE projectId = :projectId ORDER BY layerIndex ASC")
    suspend fun getLayersForProjectSync(projectId: String): List<ProjectLayerEntity>

    @Query("SELECT * FROM project_layers WHERE layerId = :layerId LIMIT 1")
    suspend fun getLayerById(layerId: String): ProjectLayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayer(layer: ProjectLayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayers(layers: List<ProjectLayerEntity>)

    @Update
    suspend fun updateLayer(layer: ProjectLayerEntity)

    @Delete
    suspend fun deleteLayer(layer: ProjectLayerEntity)

    @Query("DELETE FROM project_layers WHERE projectId = :projectId")
    suspend fun deleteLayersByProjectId(projectId: String)

    @Query("UPDATE project_layers SET isVisible = :isVisible, updatedAt = :updatedAt WHERE layerId = :layerId")
    suspend fun setLayerVisibility(layerId: String, isVisible: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE project_layers SET isLocked = :isLocked, updatedAt = :updatedAt WHERE layerId = :layerId")
    suspend fun setLayerLock(layerId: String, isLocked: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE project_layers SET isMuted = :isMuted, updatedAt = :updatedAt WHERE layerId = :layerId")
    suspend fun setLayerMute(layerId: String, isMuted: Boolean, updatedAt: Long = System.currentTimeMillis())
}
