package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AspectRatio
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportResolution
import com.example.data.model.ExportVideoCodec
import com.example.data.model.SocialPlatform

@Entity(tableName = "export_presets")
data class ExportPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val platformName: String,
    val aspectRatioName: String,
    val resolutionName: String,
    val fps: Int,
    val bitrateName: String,
    val videoCodecName: String = ExportVideoCodec.H264.name,
    val notes: String = "",
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val platform: SocialPlatform
        get() = runCatching { SocialPlatform.valueOf(platformName) }.getOrDefault(SocialPlatform.CUSTOM)

    val aspectRatio: AspectRatio
        get() = runCatching { AspectRatio.valueOf(aspectRatioName) }.getOrDefault(AspectRatio.VERTICAL_9_16)

    val resolution: ExportResolution
        get() = runCatching { ExportResolution.valueOf(resolutionName) }.getOrDefault(ExportResolution.RES_1080P)

    val bitrate: ExportBitrate
        get() = runCatching { ExportBitrate.valueOf(bitrateName) }.getOrDefault(ExportBitrate.HIGH_10M)

    val videoCodec: ExportVideoCodec
        get() = runCatching { ExportVideoCodec.valueOf(videoCodecName) }.getOrDefault(ExportVideoCodec.H264)

    fun summarySpec(): String {
        val ratioShort = aspectRatio.label.split(" ").first()
        val resShort = resolution.label.split(" ").first()
        return "$ratioShort • $resShort • ${fps}fps • ${bitrate.mbps}Mbps • ${videoCodec.shortLabel}"
    }
}
