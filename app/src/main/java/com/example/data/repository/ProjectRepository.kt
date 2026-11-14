package com.example.data.repository

import com.example.data.db.ImportedMediaDao
import com.example.data.db.ImportedMediaEntity
import com.example.data.db.ProjectDao
import com.example.data.db.ProjectEditHistoryDao
import com.example.data.db.ProjectEditHistoryEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectLayerDao
import com.example.data.db.ProjectLayerEntity
import com.example.data.db.TimelineData
import com.example.data.db.TimelineSerializer
import com.example.data.model.AspectRatio
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrackItem
import com.example.data.model.ExportResolution
import com.example.data.model.OverlaySticker
import com.example.data.model.SampleMediaRepository
import com.example.data.model.TextOverlay
import com.example.data.model.TextStylePreset
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.data.model.VideoFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val importedMediaDao: ImportedMediaDao? = null,
    private val projectLayerDao: ProjectLayerDao? = null,
    private val projectEditHistoryDao: ProjectEditHistoryDao? = null
) {

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allImportedMedia: Flow<List<ImportedMediaEntity>> = importedMediaDao?.getAllMedia() ?: emptyFlow()

    suspend fun getProject(id: String): ProjectEntity? {
        return projectDao.getProjectById(id)
    }

    suspend fun saveProject(project: ProjectEntity) {
        val existing = projectDao.getProjectById(project.id)
        if (existing != null) {
            projectDao.updateProject(project)
        } else {
            projectDao.insertProject(project)
        }
    }

    suspend fun renameProject(id: String, newTitle: String) {
        projectDao.renameProject(id, newTitle)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun addImportedMedia(media: ImportedMediaEntity) {
        importedMediaDao?.insertMedia(media)
    }

    suspend fun deleteImportedMedia(id: String) {
        importedMediaDao?.deleteMediaById(id)
    }

    fun getImportedMediaByType(type: String): Flow<List<ImportedMediaEntity>> {
        return importedMediaDao?.getMediaByType(type) ?: emptyFlow()
    }

    // --- Project Layers Methods ---
    fun getLayersForProject(projectId: String): Flow<List<ProjectLayerEntity>> {
        return projectLayerDao?.getLayersForProject(projectId) ?: emptyFlow()
    }

    suspend fun getLayersForProjectSync(projectId: String): List<ProjectLayerEntity> {
        return projectLayerDao?.getLayersForProjectSync(projectId) ?: emptyList()
    }

    suspend fun saveProjectLayers(projectId: String, layers: List<ProjectLayerEntity>) {
        projectLayerDao?.deleteLayersByProjectId(projectId)
        projectLayerDao?.insertLayers(layers)
    }

    suspend fun setLayerVisibility(layerId: String, isVisible: Boolean) {
        projectLayerDao?.setLayerVisibility(layerId, isVisible)
    }

    suspend fun setLayerLock(layerId: String, isLocked: Boolean) {
        projectLayerDao?.setLayerLock(layerId, isLocked)
    }

    suspend fun setLayerMute(layerId: String, isMuted: Boolean) {
        projectLayerDao?.setLayerMute(layerId, isMuted)
    }

    // --- Project Edit History Methods ---
    fun getEditHistoryForProject(projectId: String): Flow<List<ProjectEditHistoryEntity>> {
        return projectEditHistoryDao?.getHistoryForProject(projectId) ?: emptyFlow()
    }

    suspend fun getEditHistoryList(projectId: String): List<ProjectEditHistoryEntity> {
        return projectEditHistoryDao?.getHistoryListForProject(projectId) ?: emptyList()
    }

    suspend fun recordHistoryStep(
        projectId: String,
        stepIndex: Int,
        actionType: String,
        actionDescription: String,
        timelineJson: String,
        playheadMs: Long = 0L
    ) {
        projectEditHistoryDao?.deleteHistoryAfterStep(projectId, stepIndex)
        projectEditHistoryDao?.clearCurrentStep(projectId)
        val entry = ProjectEditHistoryEntity(
            historyId = UUID.randomUUID().toString(),
            projectId = projectId,
            stepIndex = stepIndex,
            actionType = actionType,
            actionDescription = actionDescription,
            timestamp = System.currentTimeMillis(),
            timelineSnapshotJson = timelineJson,
            playheadPositionMs = playheadMs,
            isCurrentStep = true
        )
        projectEditHistoryDao?.insertHistoryEntry(entry)
    }

    suspend fun setCurrentHistoryStep(projectId: String, historyId: String) {
        projectEditHistoryDao?.clearCurrentStep(projectId)
        projectEditHistoryDao?.setCurrentStep(historyId)
    }

    suspend fun clearHistoryForProject(projectId: String) {
        projectEditHistoryDao?.clearHistoryForProject(projectId)
    }

    suspend fun createNewProject(
        title: String,
        aspectRatio: AspectRatio = AspectRatio.VERTICAL_9_16,
        resolution: ExportResolution = ExportResolution.RES_1080P,
        fps: Int = 30,
        withSampleContent: Boolean = false
    ): ProjectEntity {
        val id = UUID.randomUUID().toString()
        val timelineData = if (withSampleContent) {
            val sample1 = SampleMediaRepository.sampleVideos[0]
            val sample2 = SampleMediaRepository.sampleVideos[1]
            val sampleAudio = SampleMediaRepository.sampleAudioTracks[0]

            val clip1 = VideoClip(
                id = UUID.randomUUID().toString(),
                name = sample1.name,
                uri = "sample://video_0",
                sourceDurationMs = sample1.durationMs,
                trimInMs = 0L,
                trimOutMs = sample1.durationMs,
                timelineStartMs = 0L,
                filter = VideoFilter.CINEMATIC,
                transitionIn = TransitionType.NONE,
                accentColorHex = sample1.colorGradientHex.second
            )

            val clip2 = VideoClip(
                id = UUID.randomUUID().toString(),
                name = sample2.name,
                uri = "sample://video_1",
                sourceDurationMs = sample2.durationMs,
                trimInMs = 0L,
                trimOutMs = sample2.durationMs,
                timelineStartMs = clip1.trimmedDurationMs,
                filter = VideoFilter.GOLDEN_HOUR,
                transitionIn = TransitionType.CROSSFADE,
                accentColorHex = sample2.colorGradientHex.second
            )

            val audio = AudioTrackItem(
                id = UUID.randomUUID().toString(),
                name = sampleAudio.name,
                timelineStartMs = 0L,
                durationMs = clip1.trimmedDurationMs + clip2.trimmedDurationMs,
                volume = 0.8f,
                category = AudioCategory.MUSIC
            )

            val text = TextOverlay(
                id = UUID.randomUUID().toString(),
                text = "OpenCut Cinema",
                timelineStartMs = 500L,
                durationMs = 3500L,
                positionX = 0.5f,
                positionY = 0.2f,
                preset = TextStylePreset.CINEMA_TITLE
            )

            val sticker = OverlaySticker(
                id = UUID.randomUUID().toString(),
                label = "Fire",
                emojiOrIcon = "🔥",
                timelineStartMs = 1000L,
                durationMs = 3000L,
                positionX = 0.8f,
                positionY = 0.25f,
                scale = 1.2f
            )

            val voiceover = AudioTrackItem(
                id = UUID.randomUUID().toString(),
                name = "Documentary Voiceover",
                timelineStartMs = 1500L,
                durationMs = 4500L,
                volume = 1.0f,
                category = AudioCategory.VOICEOVER,
                accentColorHex = 0xFFFFB300
            )

            TimelineData(
                clips = listOf(clip1, clip2),
                audioClips = listOf(audio, voiceover),
                textOverlays = listOf(text),
                stickers = listOf(sticker)
            )
        } else {
            TimelineData(emptyList(), emptyList(), emptyList(), emptyList())
        }

        val totalDuration = timelineData.clips.sumOf { it.trimmedDurationMs }
        val serialized = TimelineSerializer.serialize(timelineData)

        val entity = ProjectEntity(
            id = id,
            title = title,
            aspectRatioName = aspectRatio.name,
            resolutionName = resolution.name,
            fps = fps,
            durationMs = totalDuration,
            clipCount = timelineData.clips.size,
            updatedAt = System.currentTimeMillis(),
            thumbnailGradientStart = 0xFF1E1B4B,
            thumbnailGradientEnd = 0xFF00E5FF,
            timelineDataJson = serialized,
            createdAt = System.currentTimeMillis(),
            lastPlayheadPositionMs = 0L,
            currentWorkspace = "EDIT"
        )

        projectDao.insertProject(entity)

        // Seed initial Project Layers in Room
        val defaultLayers = listOf(
            ProjectLayerEntity(
                layerId = "$id-layer-video",
                projectId = id,
                layerIndex = 0,
                layerType = "VIDEO",
                layerName = "Main Video Track",
                itemCount = timelineData.clips.size
            ),
            ProjectLayerEntity(
                layerId = "$id-layer-audio-vo",
                projectId = id,
                layerIndex = 1,
                layerType = "AUDIO",
                layerName = "Voiceover & Dialogue",
                itemCount = timelineData.audioClips.count { it.category == AudioCategory.VOICEOVER }
            ),
            ProjectLayerEntity(
                layerId = "$id-layer-audio-bgm",
                projectId = id,
                layerIndex = 2,
                layerType = "AUDIO",
                layerName = "Background Music",
                itemCount = timelineData.audioClips.count { it.category == AudioCategory.MUSIC }
            ),
            ProjectLayerEntity(
                layerId = "$id-layer-text",
                projectId = id,
                layerIndex = 3,
                layerType = "TEXT",
                layerName = "Subtitles & Text Overlays",
                itemCount = timelineData.textOverlays.size
            ),
            ProjectLayerEntity(
                layerId = "$id-layer-stickers",
                projectId = id,
                layerIndex = 4,
                layerType = "STICKER",
                layerName = "Stickers & Graphic Badges",
                itemCount = timelineData.stickers.size
            ),
            ProjectLayerEntity(
                layerId = "$id-layer-shapes",
                projectId = id,
                layerIndex = 5,
                layerType = "SHAPE",
                layerName = "Vector Shapes & Annotations",
                itemCount = timelineData.shapes.size
            )
        )
        projectLayerDao?.insertLayers(defaultLayers)

        // Record initial creation checkpoint in editing history
        val initialHistory = ProjectEditHistoryEntity(
            historyId = UUID.randomUUID().toString(),
            projectId = id,
            stepIndex = 0,
            actionType = "PROJECT_CREATED",
            actionDescription = "Created project '$title'",
            timestamp = System.currentTimeMillis(),
            timelineSnapshotJson = serialized,
            playheadPositionMs = 0L,
            isCurrentStep = true
        )
        projectEditHistoryDao?.insertHistoryEntry(initialHistory)

        return entity
    }

    suspend fun duplicateProject(id: String): ProjectEntity? {
        val original = projectDao.getProjectById(id) ?: return null
        val newId = UUID.randomUUID().toString()
        val copy = original.copy(
            id = newId,
            title = "${original.title} (Copy)",
            updatedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        projectDao.insertProject(copy)

        // Copy layers
        val layers = projectLayerDao?.getLayersForProjectSync(id) ?: emptyList()
        if (layers.isNotEmpty()) {
            val copiedLayers = layers.map { layer ->
                layer.copy(
                    layerId = UUID.randomUUID().toString(),
                    projectId = newId,
                    updatedAt = System.currentTimeMillis()
                )
            }
            projectLayerDao?.insertLayers(copiedLayers)
        }

        // Copy history checkpoint
        val initialHistory = ProjectEditHistoryEntity(
            historyId = UUID.randomUUID().toString(),
            projectId = newId,
            stepIndex = 0,
            actionType = "PROJECT_DUPLICATED",
            actionDescription = "Duplicated from '${original.title}'",
            timestamp = System.currentTimeMillis(),
            timelineSnapshotJson = original.timelineDataJson,
            playheadPositionMs = original.lastPlayheadPositionMs,
            isCurrentStep = true
        )
        projectEditHistoryDao?.insertHistoryEntry(initialHistory)

        return copy
    }
}
