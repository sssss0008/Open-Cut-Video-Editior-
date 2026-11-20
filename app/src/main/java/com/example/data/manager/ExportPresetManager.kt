package com.example.data.manager

import android.content.Context
import com.example.data.db.ExportPresetDao
import com.example.data.db.ExportPresetEntity
import com.example.data.db.OpenCutDatabase
import com.example.data.model.AspectRatio
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportResolution
import com.example.data.model.ExportVideoCodec
import com.example.data.model.SocialPlatform
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Room-backed ExportPresetManager for saving, updating, querying, and deleting
 * custom and platform-optimized video export profiles (e.g., "Instagram Reel", "YouTube 4K")
 * storing resolution, bitrate, frame rate, aspect ratio, and video codec settings.
 */
class ExportPresetManager(
    private val exportPresetDao: ExportPresetDao
) {
    constructor(database: OpenCutDatabase) : this(database.exportPresetDao())
    constructor(context: Context) : this(OpenCutDatabase.getDatabase(context))

    val allPresets: Flow<List<ExportPresetEntity>> = exportPresetDao.getAllPresets()

    fun observePresetsForPlatform(platform: SocialPlatform): Flow<List<ExportPresetEntity>> {
        return exportPresetDao.getPresetsByPlatform(platform.name)
    }

    fun observeCustomPresets(): Flow<List<ExportPresetEntity>> {
        return exportPresetDao.getCustomPresets()
    }

    suspend fun getAllPresetsSync(): List<ExportPresetEntity> {
        return exportPresetDao.getAllPresetsSync()
    }

    suspend fun getPresetById(id: String): ExportPresetEntity? {
        return exportPresetDao.getPresetById(id)
    }

    suspend fun getPresetByName(name: String): ExportPresetEntity? {
        return exportPresetDao.getPresetByName(name.trim())
    }

    suspend fun saveCustomProfile(
        name: String,
        platform: SocialPlatform = SocialPlatform.CUSTOM,
        aspectRatio: AspectRatio = platform.recommendedAspectRatio,
        resolution: ExportResolution = platform.recommendedResolution,
        fps: Int = platform.recommendedFps,
        bitrate: ExportBitrate = platform.recommendedBitrate,
        codec: ExportVideoCodec = ExportVideoCodec.H264,
        notes: String = "",
        existingId: String? = null
    ): ExportPresetEntity {
        val now = System.currentTimeMillis()
        val trimmedName = name.trim().ifBlank {
            "${platform.displayName} ${resolution.label.split(" ").first()}"
        }
        val existing = existingId?.let { exportPresetDao.getPresetById(it) }
        val entity = ExportPresetEntity(
            id = existingId ?: "custom-${UUID.randomUUID()}",
            name = trimmedName,
            platformName = platform.name,
            aspectRatioName = aspectRatio.name,
            resolutionName = resolution.name,
            fps = fps.coerceIn(24, 60),
            bitrateName = bitrate.name,
            videoCodecName = codec.name,
            notes = notes.trim(),
            isBuiltIn = false,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        exportPresetDao.insertPreset(entity)
        return entity
    }

    suspend fun savePreset(preset: ExportPresetEntity) {
        exportPresetDao.insertPreset(preset)
    }

    /**
     * Deletes a custom export profile by ID. Built-in defaults are protected unless [allowBuiltIn] is true.
     */
    suspend fun deleteProfile(presetId: String, allowBuiltIn: Boolean = false): Boolean {
        val target = exportPresetDao.getPresetById(presetId) ?: return false
        if (target.isBuiltIn && !allowBuiltIn) return false
        exportPresetDao.deletePresetById(presetId)
        return true
    }

    /**
     * Seeds curated platform export profiles (including YouTube 4K and Instagram Reel) into Room
     * without overwriting user-created custom profiles.
     */
    suspend fun seedDefaultProfilesIfNeeded(): List<ExportPresetEntity> {
        val defaults = defaultPlatformProfiles()
        exportPresetDao.insertPresetsIfMissing(defaults)
        return exportPresetDao.getAllPresetsSync()
    }

    companion object {
        fun defaultPlatformProfiles(now: Long = System.currentTimeMillis()): List<ExportPresetEntity> = listOf(
            ExportPresetEntity(
                id = "preset-youtube-4k",
                name = "YouTube 4K",
                platformName = SocialPlatform.YOUTUBE.name,
                aspectRatioName = AspectRatio.LANDSCAPE_16_9.name,
                resolutionName = ExportResolution.RES_4K.name,
                fps = 60,
                bitrateName = ExportBitrate.ULTRA_20M.name,
                videoCodecName = ExportVideoCodec.HEVC.name,
                notes = "Ultra HD 2160p 60fps master for YouTube 4K",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now - 6000L
            ),
            ExportPresetEntity(
                id = "preset-youtube-1080p60",
                name = "YouTube 1080p60",
                platformName = SocialPlatform.YOUTUBE.name,
                aspectRatioName = AspectRatio.LANDSCAPE_16_9.name,
                resolutionName = ExportResolution.RES_1080P.name,
                fps = 60,
                bitrateName = ExportBitrate.HIGH_10M.name,
                videoCodecName = ExportVideoCodec.H264.name,
                notes = "Standard crisp 16:9 widescreen upload",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now - 5000L
            ),
            ExportPresetEntity(
                id = "preset-instagram-reels",
                name = "Instagram Reel",
                platformName = SocialPlatform.INSTAGRAM_REELS.name,
                aspectRatioName = AspectRatio.VERTICAL_9_16.name,
                resolutionName = ExportResolution.RES_1080P.name,
                fps = 30,
                bitrateName = ExportBitrate.HIGH_10M.name,
                videoCodecName = ExportVideoCodec.H264.name,
                notes = "Optimized 9:16 30fps avoids IG re-compression",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now - 4000L
            ),
            ExportPresetEntity(
                id = "preset-tiktok-vertical",
                name = "TikTok Viral HD",
                platformName = SocialPlatform.TIKTOK.name,
                aspectRatioName = AspectRatio.VERTICAL_9_16.name,
                resolutionName = ExportResolution.RES_1080P.name,
                fps = 60,
                bitrateName = ExportBitrate.HIGH_10M.name,
                videoCodecName = ExportVideoCodec.H264.name,
                notes = "Smooth 9:16 vertical 60fps for TikTok feed",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now - 3000L
            ),
            ExportPresetEntity(
                id = "preset-youtube-shorts",
                name = "YouTube Shorts",
                platformName = SocialPlatform.YOUTUBE_SHORTS.name,
                aspectRatioName = AspectRatio.VERTICAL_9_16.name,
                resolutionName = ExportResolution.RES_1080P.name,
                fps = 60,
                bitrateName = ExportBitrate.HIGH_10M.name,
                videoCodecName = ExportVideoCodec.H264.name,
                notes = "High-frame-rate vertical Shorts delivery",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now - 2000L
            ),
            ExportPresetEntity(
                id = "preset-instagram-feed",
                name = "IG 4:5 Portrait",
                platformName = SocialPlatform.INSTAGRAM_FEED.name,
                aspectRatioName = AspectRatio.PORTRAIT_4_5.name,
                resolutionName = ExportResolution.RES_1080P.name,
                fps = 30,
                bitrateName = ExportBitrate.MEDIUM_5M.name,
                videoCodecName = ExportVideoCodec.H264.name,
                notes = "Max screen height for Instagram feed posts",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now - 1000L
            ),
            ExportPresetEntity(
                id = "preset-x-fast",
                name = "X / LinkedIn Fast",
                platformName = SocialPlatform.X_TWITTER.name,
                aspectRatioName = AspectRatio.LANDSCAPE_16_9.name,
                resolutionName = ExportResolution.RES_720P.name,
                fps = 30,
                bitrateName = ExportBitrate.MEDIUM_5M.name,
                videoCodecName = ExportVideoCodec.H264.name,
                notes = "Compact 720p fast upload for X & LinkedIn",
                isBuiltIn = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
