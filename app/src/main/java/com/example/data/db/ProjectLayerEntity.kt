package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing layer/track metadata for a video project.
 * Captures layer order, type (VIDEO, AUDIO, TEXT, STICKER, SHAPE),
 * visibility, lock state, mute, volume, opacity, and blend modes.
 */
@Entity(
    tableName = "project_layers",
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
        Index(value = ["projectId", "layerIndex"])
    ]
)
data class ProjectLayerEntity(
    @PrimaryKey
    val layerId: String,
    val projectId: String,
    val layerIndex: Int, // 0 for primary video, 1 for PiP, 2 for text, etc.
    val layerType: String, // "VIDEO", "AUDIO", "TEXT", "STICKER", "SHAPE"
    val layerName: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val opacity: Float = 1.0f,
    val blendMode: String = "NORMAL",
    val itemCount: Int = 0,
    val metadataJson: String = "{}",
    val updatedAt: Long = System.currentTimeMillis()
)
