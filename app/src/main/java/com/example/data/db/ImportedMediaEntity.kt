package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imported_media")
data class ImportedMediaEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val uri: String,
    val mediaType: String, // "VIDEO", "IMAGE", "AUDIO"
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val mimeType: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val frameRate: Float = 0f,
    val rotation: Int = 0,
    val videoCodec: String = "",
    val audioInfo: String = "",
    val thumbnailPath: String = "",
    val isPlaybackVerified: Boolean = false
)
