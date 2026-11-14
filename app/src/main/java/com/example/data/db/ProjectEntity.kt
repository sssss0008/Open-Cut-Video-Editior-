package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val aspectRatioName: String,
    val resolutionName: String,
    val fps: Int,
    val durationMs: Long,
    val clipCount: Int,
    val updatedAt: Long,
    val thumbnailGradientStart: Long,
    val thumbnailGradientEnd: Long,
    val timelineDataJson: String,
    val createdAt: Long = updatedAt,
    val lastPlayheadPositionMs: Long = 0L,
    val currentWorkspace: String = "EDIT"
)
