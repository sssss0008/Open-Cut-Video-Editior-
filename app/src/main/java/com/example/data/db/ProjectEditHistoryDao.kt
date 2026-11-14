package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectEditHistoryDao {
    @Query("SELECT * FROM project_edit_history WHERE projectId = :projectId ORDER BY stepIndex ASC")
    fun getHistoryForProject(projectId: String): Flow<List<ProjectEditHistoryEntity>>

    @Query("SELECT * FROM project_edit_history WHERE projectId = :projectId ORDER BY stepIndex ASC")
    suspend fun getHistoryListForProject(projectId: String): List<ProjectEditHistoryEntity>

    @Query("SELECT * FROM project_edit_history WHERE historyId = :historyId LIMIT 1")
    suspend fun getHistoryEntryById(historyId: String): ProjectEditHistoryEntity?

    @Query("SELECT * FROM project_edit_history WHERE projectId = :projectId AND isCurrentStep = 1 LIMIT 1")
    suspend fun getCurrentStepForProject(projectId: String): ProjectEditHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntry(entry: ProjectEditHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntries(entries: List<ProjectEditHistoryEntity>)

    @Update
    suspend fun updateHistoryEntry(entry: ProjectEditHistoryEntity)

    @Delete
    suspend fun deleteHistoryEntry(entry: ProjectEditHistoryEntity)

    @Query("DELETE FROM project_edit_history WHERE projectId = :projectId AND stepIndex > :stepIndex")
    suspend fun deleteHistoryAfterStep(projectId: String, stepIndex: Int)

    @Query("DELETE FROM project_edit_history WHERE projectId = :projectId")
    suspend fun clearHistoryForProject(projectId: String)

    @Query("UPDATE project_edit_history SET isCurrentStep = 0 WHERE projectId = :projectId")
    suspend fun clearCurrentStep(projectId: String)

    @Query("UPDATE project_edit_history SET isCurrentStep = 1 WHERE historyId = :historyId")
    suspend fun setCurrentStep(historyId: String)

    @Query("SELECT COUNT(*) FROM project_edit_history WHERE projectId = :projectId")
    suspend fun getHistoryCount(projectId: String): Int
}
