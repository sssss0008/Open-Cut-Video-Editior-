package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing editing history checkpoints for a video project.
 * Enables persistent undo/redo history, state navigation, and restoring
 * exact past edits across app restarts.
 */
@Entity(
    tableName = "project_edit_history",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["projectId", "stepIndex"])
    ]
)
data class ProjectEditHistoryEntity(
    @PrimaryKey
    val historyId: String,
    val projectId: String,
    val stepIndex: Int,
    val actionType: String, // "SPLIT_CLIP", "TRIM_CLIP", "COLOR_GRADE", "APPLY_LUT", "AUTO_DUCKING", "AI_SILENCE_CUT", "ADD_TEXT", "KEYFRAME_EDIT", etc.
    val actionDescription: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timelineSnapshotJson: String,
    val playheadPositionMs: Long = 0L,
    val isCurrentStep: Boolean = false
)
