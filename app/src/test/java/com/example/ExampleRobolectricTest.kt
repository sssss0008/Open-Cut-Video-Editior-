package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.ImportedMediaEntity
import com.example.data.db.OpenCutDatabase
import com.example.data.db.ProjectEntity
import com.example.data.db.TimelineData
import com.example.data.db.TimelineSerializer
import com.example.data.model.ExportProgressData
import com.example.data.model.ExportResolution
import com.example.data.model.ExportStage
import com.example.data.model.TransitionType
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrackItem
import com.example.data.model.VideoClip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OpenCut", appName)
  }

  @Test
  fun `timeline serializer round trip`() {
    val clip = VideoClip(
      id = "test-clip-1",
      name = "Sample Video",
      uri = "sample://video_0",
      sourceDurationMs = 5000L,
      trimInMs = 500L,
      trimOutMs = 4500L,
      timelineStartMs = 0L,
      speed = 1.0f,
      transitionIn = TransitionType.WIPE_LEFT
    )
    val original = TimelineData(
      clips = listOf(clip),
      audioClips = emptyList(),
      textOverlays = emptyList(),
      stickers = emptyList()
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    assertEquals("test-clip-1", restored.clips[0].id)
    assertEquals(4000L, restored.clips[0].trimmedDurationMs)
    assertEquals(TransitionType.WIPE_LEFT, restored.clips[0].transitionIn)
  }

  @Test
  fun `transition library has full collection`() {
    val transitions = TransitionType.entries
    assertTrue(transitions.contains(TransitionType.CROSSFADE))
    assertTrue(transitions.contains(TransitionType.WIPE_LEFT))
    assertTrue(transitions.contains(TransitionType.WIPE_RIGHT))
    assertTrue(transitions.contains(TransitionType.SLIDE_LEFT))
    assertTrue(transitions.contains(TransitionType.SLIDE_RIGHT))
    assertTrue(transitions.contains(TransitionType.ZOOM_IN))
    assertTrue(transitions.contains(TransitionType.IRIS_CIRCLE))
    assertTrue(transitions.contains(TransitionType.GLITCH_FLASH))
  }

  @Test
  fun `export progress data calculates eta and elapsed correctly`() {
    val data = ExportProgressData(
      isExporting = true,
      progress = 0.5f,
      currentFrame = 150,
      totalFrames = 300,
      currentFps = 30f,
      stage = ExportStage.RENDERING_TRANSITIONS,
      elapsedSeconds = 5L,
      estimatedSecondsRemaining = 5L,
      resolution = ExportResolution.RES_1080P,
      fps = 30
    )

    assertEquals(50, data.progressPercent)
    assertEquals("00:05", data.formattedEta)
    assertEquals("00:05", data.formattedElapsed)
  }

  @Test
  fun `timeline serializer round trip with freeze frame, reverse, and canvas mode`() {
    val clip = VideoClip(
      id = "test-advanced-clip",
      name = "Advanced Clip",
      uri = "sample://video_1",
      sourceDurationMs = 6000L,
      trimInMs = 0L,
      trimOutMs = 6000L,
      timelineStartMs = 0L,
      isReversed = true,
      isFreezeFrame = true,
      hasKeyframe = true,
      keyframeScale = 1.45f,
      canvasBgMode = com.example.data.model.CanvasBgMode.BLUR
    )
    val original = TimelineData(
      clips = listOf(clip),
      audioClips = emptyList(),
      textOverlays = emptyList(),
      stickers = emptyList()
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    val restoredClip = restored.clips[0]
    assertTrue(restoredClip.isReversed)
    assertTrue(restoredClip.isFreezeFrame)
    assertTrue(restoredClip.hasKeyframe)
    assertEquals(1.45f, restoredClip.keyframeScale, 0.01f)
    assertEquals(com.example.data.model.CanvasBgMode.BLUR, restoredClip.canvasBgMode)
  }

  @Test
  fun `timeline serializer round trip with chroma key, beat sync, and animated stickers`() {
    val clip = VideoClip(
      id = "test-chroma-clip",
      name = "Chroma Clip",
      uri = "sample://video_2",
      sourceDurationMs = 8000L,
      isChromaKeyEnabled = true,
      chromaKeyColorHex = 0xFF00FF00,
      chromaKeyTolerance = 0.52f
    )
    val audio = com.example.data.model.AudioTrackItem(
      id = "test-beat-audio",
      name = "Beat Track",
      durationMs = 6000L,
      beatMarkersMs = listOf(500L, 1000L, 1500L, 2000L),
      isBeatSyncEnabled = true
    )
    val sticker = com.example.data.model.OverlaySticker(
      id = "test-animated-sticker",
      label = "Subscribe Pill",
      emojiOrIcon = "🔔 SUBSCRIBE",
      isAnimated = true,
      animationType = "SUBSCRIBE_BADGE"
    )

    val original = TimelineData(
      clips = listOf(clip),
      audioClips = listOf(audio),
      textOverlays = emptyList(),
      stickers = listOf(sticker)
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    assertTrue(restored.clips[0].isChromaKeyEnabled)
    assertEquals(0xFF00FF00, restored.clips[0].chromaKeyColorHex)
    assertEquals(0.52f, restored.clips[0].chromaKeyTolerance, 0.01f)

    assertEquals(1, restored.audioClips.size)
    assertTrue(restored.audioClips[0].isBeatSyncEnabled)
    assertEquals(4, restored.audioClips[0].beatMarkersMs.size)
    assertEquals(500L, restored.audioClips[0].beatMarkersMs[0])

    assertEquals(1, restored.stickers.size)
    assertTrue(restored.stickers[0].isAnimated)
    assertEquals("SUBSCRIBE_BADGE", restored.stickers[0].animationType)
  }

  @Test
  fun `smpte timecode formats frame-accurate time string`() {
    // 12 seconds + 4 frames at 30 fps (133ms) -> 12133ms
    val timecode = com.example.ui.components.formatSmpteTimecode(12133L, 30)
    assertEquals("00:00:12:03", timecode)

    // Exact 12 seconds
    val exactTimecode = com.example.ui.components.formatSmpteTimecode(12000L, 30)
    assertEquals("00:00:12:00", exactTimecode)
  }

  @Test
  fun `color hex conversion round trip and validation`() {
    // 6-digit hex parsing with #
    val cyan = com.example.ui.components.parseHexToColor("#00E5FF")
    assertNotNull(cyan)
    assertEquals(0xFF00E5FFL, cyan)

    // 8-digit ARGB hex parsing
    val translucentBlue = com.example.ui.components.parseHexToColor("#803B82F6")
    assertNotNull(translucentBlue)
    assertEquals(0x803B82F6L, translucentBlue)

    // Invalid hex handling
    val invalid = com.example.ui.components.parseHexToColor("#NOTAHEX")
    assertEquals(null, invalid)

    // Formatting color to hex
    val hexFormatted = com.example.ui.components.formatColorToHex(0xFF00E5FFL, false)
    assertEquals("#00E5FF", hexFormatted)

    val hexFormattedWithAlpha = com.example.ui.components.formatColorToHex(0x803B82F6L, true)
    assertEquals("#803B82F6", hexFormattedWithAlpha)
  }

  @Test
  fun `timeline serializer round trip with hardware stabilization settings`() {
    val stabilization = com.example.data.model.StabilizationSettings(
      isEnabled = true,
      mode = com.example.data.model.StabilizationMode.HIGH_ACTION,
      smoothness = 0.85f,
      cropMarginPercent = 0.12f,
      reduceRollingShutter = true
    )
    val clip = VideoClip(
      id = "test-stab-clip",
      name = "Action Cam Clip",
      uri = "sample://action_cam",
      sourceDurationMs = 7000L,
      stabilization = stabilization
    )
    val original = TimelineData(
      clips = listOf(clip),
      audioClips = emptyList(),
      textOverlays = emptyList(),
      stickers = emptyList(),
      shapes = emptyList()
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    val restoredStab = restored.clips[0].stabilization
    assertTrue(restoredStab.isEnabled)
    assertEquals(com.example.data.model.StabilizationMode.HIGH_ACTION, restoredStab.mode)
    assertEquals(0.85f, restoredStab.smoothness, 0.01f)
    assertEquals(0.12f, restoredStab.cropMarginPercent, 0.01f)
    assertTrue(restoredStab.reduceRollingShutter)
  }

  @Test
  fun `timeline serializer round trip with vector shape overlays`() {
    val shape = com.example.data.model.ShapeOverlay(
      id = "test-shape-arrow",
      shapeType = com.example.data.model.ShapeType.POINTER_ARROW,
      timelineStartMs = 1500L,
      durationMs = 2500L,
      fillColorHex = 0xCC1E293BL,
      strokeColorHex = 0xFF00E5FFL,
      strokeWidthDp = 4f,
      isFilled = true,
      cornerRadiusDp = 8f,
      rotation = 45f,
      scale = 1.25f,
      animation = com.example.data.model.ShapeAnimation.PULSE_GLOW,
      positionX = 0.5f,
      positionY = 0.7f
    )
    val original = TimelineData(
      clips = emptyList(),
      audioClips = emptyList(),
      textOverlays = emptyList(),
      stickers = emptyList(),
      shapes = listOf(shape)
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.shapes.size)
    val restoredShape = restored.shapes[0]
    assertEquals("test-shape-arrow", restoredShape.id)
    assertEquals(com.example.data.model.ShapeType.POINTER_ARROW, restoredShape.shapeType)
    assertEquals(1500L, restoredShape.timelineStartMs)
    assertEquals(2500L, restoredShape.durationMs)
    assertEquals(0xCC1E293BL, restoredShape.fillColorHex)
    assertEquals(0xFF00E5FFL, restoredShape.strokeColorHex)
    assertEquals(4f, restoredShape.strokeWidthDp, 0.01f)
    assertTrue(restoredShape.isFilled)
    assertEquals(45f, restoredShape.rotation, 0.01f)
    assertEquals(1.25f, restoredShape.scale, 0.01f)
    assertEquals(com.example.data.model.ShapeAnimation.PULSE_GLOW, restoredShape.animation)
  }

  @Test
  fun `project dao rename project updates title and timestamp offline`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, OpenCutDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val projectDao = db.projectDao()

    val initialProject = ProjectEntity(
      id = "proj-test-1",
      title = "Initial Cut",
      aspectRatioName = "LANDSCAPE_16_9",
      resolutionName = "RES_1080P",
      fps = 30,
      durationMs = 12000L,
      clipCount = 3,
      updatedAt = 1000L,
      thumbnailGradientStart = 0xFF8A2387L,
      thumbnailGradientEnd = 0xFFE94057L,
      timelineDataJson = "{}"
    )
    projectDao.insertProject(initialProject)

    val stored = projectDao.getProjectById("proj-test-1")
    assertNotNull(stored)
    assertEquals("Initial Cut", stored?.title)

    val newTimestamp = 5000L
    projectDao.renameProject("proj-test-1", "Vlog Ep 1 Final Cut", newTimestamp)

    val renamed = projectDao.getProjectById("proj-test-1")
    assertNotNull(renamed)
    assertEquals("Vlog Ep 1 Final Cut", renamed?.title)
    assertEquals(newTimestamp, renamed?.updatedAt)

    db.close()
  }

  @Test
  fun `imported media dao handles video image and audio device imports`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, OpenCutDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val mediaDao = db.importedMediaDao()

    val video = ImportedMediaEntity(
      id = "media-v1",
      name = "drone_footage.mp4",
      uri = "content://media/external/video/media/101",
      mediaType = "VIDEO",
      durationMs = 15000L,
      sizeBytes = 25000000L
    )
    val image = ImportedMediaEntity(
      id = "media-i1",
      name = "thumbnail_cover.png",
      uri = "content://media/external/images/media/202",
      mediaType = "IMAGE",
      durationMs = 4000L,
      sizeBytes = 1500000L
    )
    val audio = ImportedMediaEntity(
      id = "media-a1",
      name = "lofi_beat.mp3",
      uri = "content://media/external/audio/media/303",
      mediaType = "AUDIO",
      durationMs = 60000L,
      sizeBytes = 5000000L
    )

    mediaDao.insertMedia(video)
    mediaDao.insertMedia(image)
    mediaDao.insertMedia(audio)

    val allMedia = mediaDao.getAllMedia().first()
    assertEquals(3, allMedia.size)

    val videoList = mediaDao.getMediaByType("VIDEO").first()
    assertEquals(1, videoList.size)
    assertEquals("drone_footage.mp4", videoList[0].name)
    assertEquals("VIDEO", videoList[0].mediaType)

    val audioList = mediaDao.getMediaByType("AUDIO").first()
    assertEquals(1, audioList.size)
    assertEquals("lofi_beat.mp3", audioList[0].name)

    // Test single item retrieval
    val fetched = mediaDao.getMediaById("media-i1")
    assertNotNull(fetched)
    assertEquals("thumbnail_cover.png", fetched?.name)

    // Delete media item
    mediaDao.deleteMediaById("media-v1")
    val remaining = mediaDao.getAllMedia().first()
    assertEquals(2, remaining.size)
    assertNull(mediaDao.getMediaById("media-v1"))

    db.close()
  }

  @Test
  fun `timeline serializer round trip with OpenShot video effects keyframes audio pan and 3D titles`() {
    val effect1 = com.example.data.model.VideoEffectItem(
      id = "effect-vignette",
      type = com.example.data.model.VideoEffectType.VIGNETTE,
      intensity = 0.85f,
      isEnabled = true
    )
    val effect2 = com.example.data.model.VideoEffectItem(
      id = "effect-shift",
      type = com.example.data.model.VideoEffectType.COLOR_SHIFT,
      intensity = 0.6f,
      isEnabled = true
    )
    val keyframe1 = com.example.data.model.OpenShotKeyframe(
      id = "kf-1",
      timeOffsetMs = 0L,
      scale = 1.0f,
      rotation = 0f,
      alpha = 1.0f,
      positionX = 0f,
      positionY = 0f,
      curve = com.example.data.model.KeyframeCurve.BEZIER
    )
    val keyframe2 = com.example.data.model.OpenShotKeyframe(
      id = "kf-2",
      timeOffsetMs = 3000L,
      scale = 1.4f,
      rotation = 45f,
      alpha = 0.8f,
      positionX = 0.2f,
      positionY = -0.1f,
      curve = com.example.data.model.KeyframeCurve.LINEAR
    )
    val clip = VideoClip(
      id = "openshot-clip-1",
      name = "OpenShot Action Cut",
      uri = "sample://video_openshot",
      sourceDurationMs = 6000L,
      effects = listOf(effect1, effect2),
      keyframes = listOf(keyframe1, keyframe2),
      alpha = 0.95f,
      positionX = 0.1f,
      positionY = -0.05f
    )
    val audio = com.example.data.model.AudioTrackItem(
      id = "openshot-audio-1",
      name = "Cinematic Score",
      durationMs = 6000L,
      volume = 0.8f,
      pan = -0.45f,
      fadeType = com.example.data.model.AudioFadeType.FAST_1S
    )
    val textOverlay = com.example.data.model.TextOverlay(
      id = "openshot-title-3d",
      text = "INTERSTELLAR ODYSSEY",
      subtitle = "DIRECTOR'S CUT",
      is3D = true,
      template = "SPACE_CRAWL_3D",
      borderWidthDp = 2.5f,
      borderColorHex = 0xFFFFD700
    )
    val original = TimelineData(
      clips = listOf(clip),
      audioClips = listOf(audio),
      textOverlays = listOf(textOverlay),
      stickers = emptyList()
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    val restoredClip = restored.clips[0]
    assertEquals(2, restoredClip.effects.size)
    assertEquals(com.example.data.model.VideoEffectType.VIGNETTE, restoredClip.effects[0].type)
    assertEquals(0.85f, restoredClip.effects[0].intensity, 0.01f)
    assertEquals(com.example.data.model.VideoEffectType.COLOR_SHIFT, restoredClip.effects[1].type)

    assertEquals(2, restoredClip.keyframes.size)
    assertEquals(com.example.data.model.KeyframeCurve.BEZIER, restoredClip.keyframes[0].curve)
    assertEquals(1.4f, restoredClip.keyframes[1].scale, 0.01f)
    assertEquals(0.95f, restoredClip.alpha, 0.01f)

    assertEquals(1, restored.audioClips.size)
    assertEquals(-0.45f, restored.audioClips[0].pan, 0.01f)
    assertEquals(com.example.data.model.AudioFadeType.FAST_1S, restored.audioClips[0].fadeType)

    assertEquals(1, restored.textOverlays.size)
    val restoredText = restored.textOverlays[0]
    assertTrue(restoredText.is3D)
    assertEquals("SPACE_CRAWL_3D", restoredText.template)
    assertEquals("DIRECTOR'S CUT", restoredText.subtitle)
    assertEquals(2.5f, restoredText.borderWidthDp, 0.01f)
  }

  @Test
  fun `OpenShot keyframe interpolation computes smooth curve`() {
    val kf1 = com.example.data.model.OpenShotKeyframe(
      timeOffsetMs = 0L,
      scale = 1.0f,
      rotation = 0f,
      alpha = 1.0f,
      positionX = 0f,
      positionY = 0f,
      curve = com.example.data.model.KeyframeCurve.BEZIER
    )
    val kf2 = com.example.data.model.OpenShotKeyframe(
      timeOffsetMs = 2000L,
      scale = 2.0f,
      rotation = 180f,
      alpha = 0.5f,
      positionX = 1.0f,
      positionY = -1.0f,
      curve = com.example.data.model.KeyframeCurve.BEZIER
    )

    val keyframes = listOf(kf1, kf2)

    // At t=0
    val startResult = com.example.ui.components.interpolateKeyframes(
      keyframes = keyframes,
      elapsedInClipMs = 0L,
      defaultScale = 1f,
      defaultRotation = 0f,
      defaultAlpha = 1f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )
    assertEquals(1.0f, startResult.scale, 0.01f)
    assertEquals(0f, startResult.rotation, 0.01f)

    // At t=1000 (midpoint of ease in-out should be 0.5)
    val midResult = com.example.ui.components.interpolateKeyframes(
      keyframes = keyframes,
      elapsedInClipMs = 1000L,
      defaultScale = 1f,
      defaultRotation = 0f,
      defaultAlpha = 1f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )
    assertEquals(1.5f, midResult.scale, 0.01f)
    assertEquals(90f, midResult.rotation, 0.01f)
    assertEquals(0.75f, midResult.alpha, 0.01f)

    // At t=2000
    val endResult = com.example.ui.components.interpolateKeyframes(
      keyframes = keyframes,
      elapsedInClipMs = 2000L,
      defaultScale = 1f,
      defaultRotation = 0f,
      defaultAlpha = 1f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )
    assertEquals(2.0f, endResult.scale, 0.01f)
    assertEquals(180f, endResult.rotation, 0.01f)
  }

  @Test
  fun `smpte timecode formatting at 30 fps produces accurate timecodes`() {
    // 0 ms -> 00:00:00:00
    assertEquals("00:00:00:00", com.example.ui.components.formatSmpteTimecode(0L, 30))

    // 1000 ms -> 00:00:01:00
    assertEquals("00:00:01:00", com.example.ui.components.formatSmpteTimecode(1000L, 30))

    // 3661500 ms -> 1h 1m 1s + 500ms (~15 frames at 30fps)
    assertEquals("01:01:01:15", com.example.ui.components.formatSmpteTimecode(3661500L, 30))
  }

  @Test
  fun `drag and drop clip horizontal reordering updates sequence and recalculates timeline ripple`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    val clips = vm.uiState.value.clips
    if (clips.size >= 2) {
      val firstClipId = clips[0].id
      val secondClipId = clips[1].id
      val firstDuration = clips[0].trimmedDurationMs

      // Drag clip 0 to drop at slot 1
      vm.reorderClips(0, 1)

      val reordered = vm.uiState.value.clips
      assertEquals(secondClipId, reordered[0].id)
      assertEquals(firstClipId, reordered[1].id)

      // The new first clip must start at 0ms
      assertEquals(0L, reordered[0].timelineStartMs)
      // The second clip must start at the end of the first clip (ripple calculation)
      assertEquals(reordered[0].trimmedDurationMs, reordered[1].timelineStartMs)
    }
  }

  @Test
  fun `precision scrubber toggle updates state properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    vm.togglePrecisionScrubber(true)
    assertTrue(vm.uiState.value.showPrecisionScrubber)

    vm.togglePrecisionScrubber(false)
    assertTrue(!vm.uiState.value.showPrecisionScrubber)
  }

  @Test
  fun `keyframe property animation across scale, rotation, opacity, and position`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Test Clip 1", 6000L)
    }
    val clip = vm.uiState.value.clips.first()

    // 1. Add keyframe at 500ms
    val kf1 = com.example.data.model.OpenShotKeyframe(
      timeOffsetMs = 500L,
      scale = 1.5f,
      rotation = 45f,
      alpha = 0.8f,
      positionX = 0.2f,
      positionY = -0.1f,
      curve = com.example.data.model.KeyframeCurve.BEZIER
    )
    vm.addKeyframe(clip.id, kf1)

    val updatedClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertTrue(updatedClip.hasKeyframe)
    assertTrue(updatedClip.keyframes.any { it.id == kf1.id })

    // 2. Update existing keyframe values
    val modifiedKf = kf1.copy(scale = 2.0f, rotation = 90f, alpha = 0.5f)
    vm.updateKeyframe(clip.id, modifiedKf)

    val clipAfterUpdate = vm.uiState.value.clips.find { it.id == clip.id }!!
    val foundKf = clipAfterUpdate.keyframes.find { it.id == kf1.id }!!
    assertEquals(2.0f, foundKf.scale, 0.001f)
    assertEquals(90f, foundKf.rotation, 0.001f)
    assertEquals(0.5f, foundKf.alpha, 0.001f)

    // 3. Remove keyframe
    vm.removeKeyframe(clip.id, kf1.id)
    val clipAfterRemove = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertTrue(clipAfterRemove.keyframes.none { it.id == kf1.id })

    // 4. Batch set keyframes and clear all
    val multiKfs = listOf(
      com.example.data.model.OpenShotKeyframe(timeOffsetMs = 0L, scale = 1.0f, alpha = 1.0f),
      com.example.data.model.OpenShotKeyframe(timeOffsetMs = 1000L, scale = 1.5f, alpha = 0.5f)
    )
    vm.setClipKeyframes(clip.id, multiKfs)
    assertEquals(2, vm.uiState.value.clips.find { it.id == clip.id }!!.keyframes.size)

    vm.clearAllKeyframes(clip.id)
    assertEquals(0, vm.uiState.value.clips.find { it.id == clip.id }!!.keyframes.size)
    assertTrue(!vm.uiState.value.clips.find { it.id == clip.id }!!.hasKeyframe)
  }

  @Test
  fun `interpolateKeyframes evaluates bezier and linear curves accurately over time`() {
    val keyframes = listOf(
      com.example.data.model.OpenShotKeyframe(
        timeOffsetMs = 0L,
        scale = 1.0f,
        rotation = 0f,
        alpha = 0.0f,
        positionX = 0f,
        positionY = 0f,
        curve = com.example.data.model.KeyframeCurve.LINEAR
      ),
      com.example.data.model.OpenShotKeyframe(
        timeOffsetMs = 1000L,
        scale = 2.0f,
        rotation = 180f,
        alpha = 1.0f,
        positionX = 0.5f,
        positionY = -0.5f,
        curve = com.example.data.model.KeyframeCurve.LINEAR
      )
    )

    // At start (0ms)
    val atStart = com.example.ui.components.interpolateKeyframes(
      keyframes = keyframes,
      elapsedInClipMs = 0L,
      defaultScale = 1.0f,
      defaultRotation = 0f,
      defaultAlpha = 1.0f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )
    assertEquals(1.0f, atStart.scale, 0.001f)
    assertEquals(0f, atStart.rotation, 0.001f)
    assertEquals(0.0f, atStart.alpha, 0.001f)

    // At exact midpoint (500ms) with Linear curve -> 50%
    val atMid = com.example.ui.components.interpolateKeyframes(
      keyframes = keyframes,
      elapsedInClipMs = 500L,
      defaultScale = 1.0f,
      defaultRotation = 0f,
      defaultAlpha = 1.0f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )
    assertEquals(1.5f, atMid.scale, 0.01f)
    assertEquals(90f, atMid.rotation, 0.01f)
    assertEquals(0.5f, atMid.alpha, 0.01f)
    assertEquals(0.25f, atMid.positionX, 0.01f)

    // At end (1000ms)
    val atEnd = com.example.ui.components.interpolateKeyframes(
      keyframes = keyframes,
      elapsedInClipMs = 1000L,
      defaultScale = 1.0f,
      defaultRotation = 0f,
      defaultAlpha = 1.0f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )
    assertEquals(2.0f, atEnd.scale, 0.001f)
    assertEquals(180f, atEnd.rotation, 0.001f)
    assertEquals(1.0f, atEnd.alpha, 0.001f)
  }

  @Test
  fun `drag and drop transition workflow sets transition on clip seam`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Test Clip 1", 6000L)
      vm.addCustomMediaClip("sample://2", "Test Clip 2", 6000L)
    }
    val targetClip = vm.uiState.value.clips.first()

    // 1. Begin dragging a transition (e.g. Crossfade)
    vm.startDraggingTransition(com.example.data.model.TransitionType.CROSSFADE, durationMs = 800L)
    assertEquals(com.example.data.model.TransitionType.CROSSFADE, vm.uiState.value.draggingTransition)
    assertEquals(800L, vm.uiState.value.transitionDragDurationMs)

    // 2. Drop transition onto the clip cut target
    vm.dropTransitionOnClip(targetClip.id)
    assertEquals(null, vm.uiState.value.draggingTransition)

    val updatedClip = vm.uiState.value.clips.find { it.id == targetClip.id }!!
    assertEquals(com.example.data.model.TransitionType.CROSSFADE, updatedClip.transitionIn)
    assertEquals(800L, updatedClip.transitionDurationMs)

    // 3. Test apply transition to all clips
    vm.applyTransitionToAllClips(com.example.data.model.TransitionType.WIPE_LEFT, 500L)
    assertTrue(vm.uiState.value.clips.all { it.transitionIn == com.example.data.model.TransitionType.WIPE_LEFT })
    assertTrue(vm.uiState.value.clips.all { it.transitionDurationMs == 500L })
  }

  @Test
  fun `timeline magnetic snapping engine snaps to playhead, cuts, markers, and zero`() {
    val clip1 = VideoClip(
      id = "clip-1",
      name = "Intro",
      uri = "sample://1",
      sourceDurationMs = 5000L,
      timelineStartMs = 0L,
      trimInMs = 0L,
      trimOutMs = 5000L
    )
    val clip2 = VideoClip(
      id = "clip-2",
      name = "Main",
      uri = "sample://2",
      sourceDurationMs = 6000L,
      timelineStartMs = 5000L,
      trimInMs = 0L,
      trimOutMs = 6000L
    )
    val clips = listOf(clip1, clip2)
    val markers = listOf(
      com.example.data.model.TimelineCueMarker(timeMs = 2500L, label = "Beat Drop"),
      com.example.data.model.TimelineCueMarker(timeMs = 8000L, label = "Voice In")
    )

    // 1. Snapping to 00:00 (Start)
    val snapStart = com.example.ui.components.calculateSnapTarget(
      rawTimeMs = 40L,
      timelineZoom = 1.0f,
      currentPositionMs = 3000L,
      clips = clips,
      timelineMarkers = markers,
      totalDurationMs = 11000L,
      isSnappingEnabled = true
    )
    assertNotNull(snapStart)
    assertEquals(0L, snapStart!!.timeMs)
    assertEquals(com.example.data.model.SnapTargetType.START_ZERO, snapStart.type)

    // 2. Snapping to Playhead (3000ms)
    val snapPlayhead = com.example.ui.components.calculateSnapTarget(
      rawTimeMs = 3050L,
      timelineZoom = 1.0f,
      currentPositionMs = 3000L,
      clips = clips,
      timelineMarkers = markers,
      totalDurationMs = 11000L,
      isSnappingEnabled = true
    )
    assertNotNull(snapPlayhead)
    assertEquals(3000L, snapPlayhead!!.timeMs)
    assertEquals(com.example.data.model.SnapTargetType.PLAYHEAD, snapPlayhead.type)

    // 3. Snapping to Cut Seam (5000ms)
    val snapCut = com.example.ui.components.calculateSnapTarget(
      rawTimeMs = 4960L,
      timelineZoom = 1.0f,
      currentPositionMs = 1000L,
      clips = clips,
      timelineMarkers = markers,
      totalDurationMs = 11000L,
      isSnappingEnabled = true
    )
    assertNotNull(snapCut)
    assertEquals(5000L, snapCut!!.timeMs)
    assertEquals(com.example.data.model.SnapTargetType.CUT_POINT, snapCut.type)

    // 4. Snapping to Cue Marker (2500ms)
    val snapMarker = com.example.ui.components.calculateSnapTarget(
      rawTimeMs = 2520L,
      timelineZoom = 1.0f,
      currentPositionMs = 1000L,
      clips = clips,
      timelineMarkers = markers,
      totalDurationMs = 11000L,
      isSnappingEnabled = true
    )
    assertNotNull(snapMarker)
    assertEquals(2500L, snapMarker!!.timeMs)
    assertEquals(com.example.data.model.SnapTargetType.CUE_MARKER, snapMarker.type)

    // 5. Disabled Snapping returns null
    val disabledSnap = com.example.ui.components.calculateSnapTarget(
      rawTimeMs = 4960L,
      timelineZoom = 1.0f,
      currentPositionMs = 1000L,
      clips = clips,
      timelineMarkers = markers,
      totalDurationMs = 11000L,
      isSnappingEnabled = false
    )
    assertNull(disabledSnap)
  }

  @Test
  fun `track headers visibility and lock toggles update state`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    // Initially all tracks unlocked and visible
    assertEquals(false, vm.uiState.value.isVideoTrackMuted)
    assertEquals(false, vm.uiState.value.isVideoTrackLocked)
    assertEquals(false, vm.uiState.value.isAudioTrackMuted)
    assertEquals(false, vm.uiState.value.isAudioTrackLocked)

    // Toggle Video Track
    vm.toggleTrackVisibility(com.example.data.model.TrackType.VIDEO)
    assertEquals(true, vm.uiState.value.isVideoTrackMuted)
    vm.toggleTrackLock(com.example.data.model.TrackType.VIDEO)
    assertEquals(true, vm.uiState.value.isVideoTrackLocked)

    // Toggle Audio Track
    vm.toggleTrackVisibility(com.example.data.model.TrackType.AUDIO)
    assertEquals(true, vm.uiState.value.isAudioTrackMuted)
    vm.toggleTrackLock(com.example.data.model.TrackType.AUDIO)
    assertEquals(true, vm.uiState.value.isAudioTrackLocked)

    // Toggle Overlay / Sticker Track
    vm.toggleTrackVisibility(com.example.data.model.TrackType.OVERLAY)
    assertEquals(false, vm.uiState.value.isStickerTrackVisible)
    vm.toggleTrackLock(com.example.data.model.TrackType.OVERLAY)
    assertEquals(true, vm.uiState.value.isStickerTrackLocked)
  }

  @Test
  fun `timeline cue markers can be added, jumped to, and deleted`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Test Clip 1", 6000L)
    }

    val initialCount = vm.uiState.value.timelineMarkers.size

    // Add marker at 3500ms
    vm.addTimelineCueMarker(timeMs = 3500L, label = "Intro Solo")
    assertEquals(initialCount + 1, vm.uiState.value.timelineMarkers.size)
    val marker = vm.uiState.value.timelineMarkers.find { it.label == "Intro Solo" }
    assertNotNull(marker)
    assertEquals(3500L, marker!!.timeMs)

    // Jump to marker
    vm.jumpToMarker(marker)
    assertEquals(3500L, vm.uiState.value.currentPositionMs)

    // Delete marker
    vm.deleteTimelineCueMarker(marker.id)
    assertEquals(initialCount, vm.uiState.value.timelineMarkers.size)
  }

  @Test
  fun `drag and drop clip insertion onto timeline re-offsets and updates sequence`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    val sample = com.example.data.model.SampleMediaRepository.sampleVideos.first()
    val initialClipsCount = vm.uiState.value.clips.size

    // Insert clip at beginning (index 0)
    vm.insertClipAtIndex(sample, 0)
    assertEquals(initialClipsCount + 1, vm.uiState.value.clips.size)
    val firstClip = vm.uiState.value.clips.first()
    assertEquals(sample.name, firstClip.name)
    assertEquals(0L, firstClip.timelineStartMs)

    // Insert another sample clip at specific timestamp
    val secondSample = com.example.data.model.SampleMediaRepository.sampleVideos[1]
    vm.insertClipAtTime(secondSample, targetTimeMs = 4000L)
    assertEquals(initialClipsCount + 2, vm.uiState.value.clips.size)

    // Verify clips maintain sequential contiguous timeline layout
    val allClips = vm.uiState.value.clips
    for (i in 0 until allClips.size - 1) {
      assertEquals(allClips[i].timelineEndMs, allClips[i + 1].timelineStartMs)
    }
  }

  @Test
  fun `interactive trimmer adjusts start in-point and end out-point of clip`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.addCustomMediaClip("sample://trim1", "Trim Test Clip", 8000L)
    val clip = vm.uiState.value.clips.last()

    assertEquals(0L, clip.trimInMs)
    assertEquals(8000L, clip.trimOutMs)
    assertEquals(8000L, clip.trimmedDurationMs)

    // Adjust in-point to 1500ms and out-point to 6500ms
    vm.updateTrim(clip.id, 1500L, 6500L)
    val trimmedClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(1500L, trimmedClip.trimInMs)
    assertEquals(6500L, trimmedClip.trimOutMs)
    assertEquals(5000L, trimmedClip.trimmedDurationMs)

    // Test live update during interactive drag
    vm.updateTrimLive(clip.id, 2000L, 6000L)
    val liveClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(2000L, liveClip.trimInMs)
    assertEquals(6000L, liveClip.trimOutMs)
    assertEquals(4000L, liveClip.trimmedDurationMs)

    // Test micro-stepping adjustments
    vm.stepTrim(clip.id, deltaInMs = -100L, deltaOutMs = +200L)
    val steppedClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(1900L, steppedClip.trimInMs)
    assertEquals(6200L, steppedClip.trimOutMs)

    // Test reset trim
    vm.resetTrim(clip.id)
    val resetClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(0L, resetClip.trimInMs)
    assertEquals(8000L, resetClip.trimOutMs)
    assertEquals(8000L, resetClip.trimmedDurationMs)
  }

  @Test
  fun `trim adjustments enforce valid boundary constraints`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.addCustomMediaClip("sample://trim2", "Bounds Clip", 5000L)
    val clip = vm.uiState.value.clips.last()

    // Negative in-point should coerce to 0L
    vm.updateTrim(clip.id, -500L, 4000L)
    var result = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(0L, result.trimInMs)
    assertEquals(4000L, result.trimOutMs)

    // Out-point greater than duration should coerce to sourceDurationMs
    vm.updateTrim(clip.id, 1000L, 9999L)
    result = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(1000L, result.trimInMs)
    assertEquals(5000L, result.trimOutMs)

    // In-point overlapping out-point should maintain minimum duration constraint
    vm.updateTrim(clip.id, 4900L, 4950L)
    result = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertTrue(result.trimmedDurationMs >= 200L)
  }

  @Test
  fun `transition library categories and applying transitions to clips and all cuts`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    // Verify categories
    assertEquals("Dissolve", TransitionType.CROSSFADE.category)
    assertEquals("Color Fade", TransitionType.DIP_TO_BLACK.category)
    assertEquals("Color Fade", TransitionType.DIP_TO_WHITE.category)
    assertEquals("Wipe", TransitionType.WIPE_LEFT.category)
    assertEquals("Slide", TransitionType.SLIDE_RIGHT.category)
    assertEquals("Zoom", TransitionType.ZOOM_IN.category)
    assertEquals("Shape", TransitionType.IRIS_CIRCLE.category)
    assertEquals("Stylized", TransitionType.GLITCH_FLASH.category)

    vm.addCustomMediaClip("sample://clip1", "Clip 1", 5000L)
    vm.addCustomMediaClip("sample://clip2", "Clip 2", 5000L)
    vm.addCustomMediaClip("sample://clip3", "Clip 3", 5000L)

    val clips = vm.uiState.value.clips
    assertTrue(clips.size >= 2)
    val secondClip = clips[1]

    // Apply transition to specific clip
    vm.setTransition(secondClip.id, TransitionType.CROSSFADE, 1200L)
    val updatedSecond = vm.uiState.value.clips.find { it.id == secondClip.id }!!
    assertEquals(TransitionType.CROSSFADE, updatedSecond.transitionIn)
    assertEquals(1200L, updatedSecond.transitionDurationMs)

    // Apply to all clips
    vm.applyTransitionToAllClips(TransitionType.WIPE_LEFT, 750L)
    val allUpdated = vm.uiState.value.clips
    for (i in 1 until allUpdated.size) {
      assertEquals(TransitionType.WIPE_LEFT, allUpdated[i].transitionIn)
      assertEquals(750L, allUpdated[i].transitionDurationMs)
    }

    // Drag-and-drop state verification
    vm.startDraggingTransition(TransitionType.IRIS_CIRCLE, 1000L)
    assertEquals(TransitionType.IRIS_CIRCLE, vm.uiState.value.draggingTransition)
    assertEquals(1000L, vm.uiState.value.transitionDragDurationMs)

    vm.stopDraggingTransition()
    assertNull(vm.uiState.value.draggingTransition)
  }

  @Test
  fun `audio mixing auto ducking attenuates background music when voiceover or dialogue active`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    // Add BGM track and Voiceover track from sample library
    val bgmSample = com.example.data.model.SampleMediaRepository.sampleAudioTracks.first { it.category == com.example.data.model.AudioCategory.MUSIC }
    vm.addAudioTrack(bgmSample)
    val bgmTrack = vm.uiState.value.audioClips.last()
    vm.setAudioTrackCategory(bgmTrack.id, com.example.data.model.AudioCategory.MUSIC)
    if (!bgmTrack.isDuckingEnabled) {
      vm.toggleAudioDucking(bgmTrack.id)
    }

    val voSample = com.example.data.model.SampleMediaRepository.sampleAudioTracks.first { it.category == com.example.data.model.AudioCategory.VOICEOVER }
    vm.addAudioTrack(voSample)
    val voTrack = vm.uiState.value.audioClips.last()
    vm.setAudioTrackCategory(voTrack.id, com.example.data.model.AudioCategory.VOICEOVER)

    // Position playhead within voiceover bounds
    vm.seekTo(voTrack.timelineStartMs + 500L)

    // Auto-ducking master is enabled by default
    assertTrue(vm.uiState.value.isAutoDuckingMasterEnabled)
    assertTrue(vm.uiState.value.isVoiceoverActive)

    val duckedBgm = vm.uiState.value.audioClips.find { it.id == bgmTrack.id }!!
    val effectiveVol = vm.uiState.value.getEffectiveTrackVolume(duckedBgm)

    // Effective volume should be ducked by autoDuckingDuckLevel (0.25f)
    val expectedVol = duckedBgm.volume * vm.uiState.value.autoDuckingDuckLevel
    assertEquals(expectedVol, effectiveVol, 0.01f)

    // When voiceover is muted, auto-ducking should not activate
    vm.setAudioTrackVolume(voTrack.id, 1.0f, isMuted = true)
    org.junit.Assert.assertFalse(vm.uiState.value.isVoiceoverActive)
    val restoredVol = vm.uiState.value.getEffectiveTrackVolume(duckedBgm)
    assertEquals(duckedBgm.volume, restoredVol, 0.01f)

    // Unmute and switch voiceover to dialogue - dialogue also triggers ducking
    vm.setAudioTrackVolume(voTrack.id, 1.0f, isMuted = false)
    vm.setAudioTrackCategory(voTrack.id, com.example.data.model.AudioCategory.DIALOGUE)
    assertTrue(vm.uiState.value.isVoiceoverActive)
    assertEquals(expectedVol, vm.uiState.value.getEffectiveTrackVolume(duckedBgm), 0.01f)

    // Disable auto-ducking master switch
    vm.setAutoDuckingMaster(false)
    org.junit.Assert.assertFalse(vm.uiState.value.isAutoDuckingMasterEnabled)
    assertEquals(duckedBgm.volume, vm.uiState.value.getEffectiveTrackVolume(duckedBgm), 0.01f)

    // Change ducking reduction level to 0.10 (-20dB) and re-enable master
    vm.setAutoDuckingLevel(0.10f)
    vm.setAutoDuckingMaster(true)
    val newExpected = duckedBgm.volume * 0.10f
    assertEquals(newExpected, vm.uiState.value.getEffectiveTrackVolume(duckedBgm), 0.01f)
  }

  @Test
  fun `canvas background processor detects vertical video on horizontal canvas and applies gaussian blur`() {
    val verticalClip = com.example.data.model.VideoClip(
      id = "vert-clip-1",
      name = "TikTok Vertical Reel",
      uri = "sample://vert",
      sourceDurationMs = 5000L,
      isVertical = true,
      canvasBgMode = com.example.data.model.CanvasBgMode.BLUR,
      canvasBlurRadiusDp = 30f
    )
    val horizontalClip = com.example.data.model.VideoClip(
      id = "horiz-clip-1",
      name = "YouTube Landscape Video",
      uri = "sample://horiz",
      sourceDurationMs = 5000L,
      isVertical = false
    )

    // 16:9 Landscape Canvas
    assertTrue(com.example.ui.components.isVerticalOnHorizontal(verticalClip, com.example.data.model.AspectRatio.LANDSCAPE_16_9))
    assertTrue(com.example.ui.components.isVerticalOnHorizontal(verticalClip, com.example.data.model.AspectRatio.CINEMATIC_21_9))

    // 9:16 Vertical Canvas (vertical video on vertical canvas should not blur pillarboxes)
    org.junit.Assert.assertFalse(com.example.ui.components.isVerticalOnHorizontal(verticalClip, com.example.data.model.AspectRatio.VERTICAL_9_16))

    // Horizontal video on 16:9 canvas should not blur
    org.junit.Assert.assertFalse(com.example.ui.components.isVerticalOnHorizontal(horizontalClip, com.example.data.model.AspectRatio.LANDSCAPE_16_9))

    // Gaussian kernel weights calculation
    val weights = com.example.ui.components.calculateGaussianKernelWeights(sigma = 5.0f, radius = 4)
    assertEquals(9, weights.size)
    var sum = 0f
    for (w in weights) {
      sum += w
    }
    assertEquals(1.0f, sum, 0.001f)

    // Test ViewModel canvas blur and vertical orientation methods
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.addCustomMediaClip("sample://clip_test", "Landscape Clip", 6000L)
    val clip = vm.uiState.value.clips.last()

    vm.setClipVerticalOrientation(clip.id, true)
    var updated = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertTrue(updated.isVertical)
    assertTrue(updated.isVerticalVideo)

    vm.setClipCanvasBlur(clip.id, 40f, 0.75f)
    updated = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(40f, updated.canvasBlurRadiusDp, 0.01f)
    assertEquals(0.75f, updated.canvasBgBrightness, 0.01f)
    assertEquals(com.example.data.model.CanvasBgMode.BLUR, updated.canvasBgMode)
  }

  @Test
  fun `keyframe parameter editor overlay adjusts scale opacity and rotation over time`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.addCustomMediaClip("sample://clip_kf", "Keyframe Test Clip", 8000L)
    val clip = vm.uiState.value.clips.last()

    // Test overlay visibility toggle
    org.junit.Assert.assertFalse(vm.uiState.value.showKeyframeOverlay)
    vm.toggleKeyframeOverlay(true)
    assertTrue(vm.uiState.value.showKeyframeOverlay)
    vm.toggleKeyframeOverlay(false)
    org.junit.Assert.assertFalse(vm.uiState.value.showKeyframeOverlay)

    // Seek to 1000ms and add/update keyframe properties
    vm.seekTo(clip.timelineStartMs + 1000L)
    vm.updateKeyframeAtPlayhead(clip.id, scale = 1.5f, opacity = 0.8f, rotation = 45f)

    var updatedClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(1, updatedClip.keyframes.size)
    val kf1 = updatedClip.keyframes[0]
    assertEquals(1000L, kf1.timeOffsetMs)
    assertEquals(1.5f, kf1.scale, 0.01f)
    assertEquals(0.8f, kf1.alpha, 0.01f)
    assertEquals(45f, kf1.rotation, 0.01f)

    // Seek to 4000ms and add second keyframe with scale, opacity, rotation over time
    vm.seekTo(clip.timelineStartMs + 4000L)
    vm.updateKeyframeAtPlayhead(clip.id, scale = 2.0f, opacity = 0.2f, rotation = 180f)

    updatedClip = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(2, updatedClip.keyframes.size)
    val kf2 = updatedClip.keyframes[1]
    assertEquals(4000L, kf2.timeOffsetMs)
    assertEquals(2.0f, kf2.scale, 0.01f)
    assertEquals(0.2f, kf2.alpha, 0.01f)
    assertEquals(180f, kf2.rotation, 0.01f)

    // Interpolate midpoint at 2500ms (50% between 1000ms and 4000ms) with Linear curve
    val linearKfs = listOf(
      kf1.copy(curve = com.example.data.model.KeyframeCurve.LINEAR),
      kf2.copy(curve = com.example.data.model.KeyframeCurve.LINEAR)
    )
    val mid = com.example.ui.components.interpolateKeyframes(
      keyframes = linearKfs,
      elapsedInClipMs = 2500L,
      defaultScale = 1.0f,
      defaultRotation = 0f,
      defaultAlpha = 1.0f,
      defaultPosX = 0f,
      defaultPosY = 0f
    )

    // Midpoint: scale = 1.75f, opacity = 0.5f, rotation = 112.5f
    assertEquals(1.75f, mid.scale, 0.02f)
    assertEquals(0.5f, mid.alpha, 0.02f)
    assertEquals(112.5f, mid.rotation, 0.5f)
  }

  @Test
  fun `draggable scrubber head timecode and smpte format`() {
    val smpteAtZero = com.example.ui.components.formatSmpteTimecode(0L)
    assertEquals("00:00:00:00", smpteAtZero)

    val smpteAt1Sec = com.example.ui.components.formatSmpteTimecode(1000L)
    assertEquals("00:00:01:00", smpteAt1Sec)

    val smpteAt1Min = com.example.ui.components.formatSmpteTimecode(60000L)
    assertEquals("00:01:00:00", smpteAt1Min)

    // Check frame number at 30fps
    val frameAt500ms = ((500L * 30L / 1000L) % 30L).toInt()
    assertEquals(15, frameAt500ms)
  }

  @Test
  fun `timeline navigation and playback speed state`() {
    val state = com.example.viewmodel.EditorUiState(
      currentPositionMs = 2500L,
      isPlaying = false,
      isScrubbing = true,
      playbackSpeed = 1.5f
    )
    assertTrue(state.isScrubbing)
    assertEquals(1.5f, state.playbackSpeed, 0.01f)
    assertEquals(2500L, state.currentPositionMs)
  }

  @Test
  fun `audio ducking lowers background music volume when voiceover track is active`() {
    val bgMusic = AudioTrackItem(
      id = "bg-music-1",
      name = "Midnight Study BGM",
      category = AudioCategory.MUSIC,
      timelineStartMs = 0L,
      durationMs = 10000L,
      volume = 0.8f
    )
    val voiceover = AudioTrackItem(
      id = "vo-track-1",
      name = "Narration Voiceover",
      category = AudioCategory.VOICEOVER,
      timelineStartMs = 2000L,
      durationMs = 4000L,
      volume = 1.0f
    )

    // Position at 1000ms: Voiceover is not active yet -> BGM is at normal 0.8f
    val stateBeforeVoiceover = com.example.viewmodel.EditorUiState(
      currentPositionMs = 1000L,
      audioClips = listOf(bgMusic, voiceover),
      isAutoDuckingMasterEnabled = true,
      autoDuckingDuckLevel = 0.25f
    )
    assertFalse(stateBeforeVoiceover.isVoiceoverActive)
    assertEquals(0.8f, stateBeforeVoiceover.getEffectiveTrackVolume(bgMusic), 0.001f)

    // Position at 3000ms: Voiceover is active -> BGM automatically ducks to 0.8 * 0.25 = 0.20
    val stateDuringVoiceover = stateBeforeVoiceover.copy(currentPositionMs = 3000L)
    assertTrue(stateDuringVoiceover.isVoiceoverActive)
    assertTrue(stateDuringVoiceover.isVoiceoverTrackSpeechActive)
    assertEquals(0.20f, stateDuringVoiceover.getEffectiveTrackVolume(bgMusic), 0.001f)

    // Position at 7000ms: Voiceover has ended -> BGM restores to normal volume 0.8f
    val stateAfterVoiceover = stateBeforeVoiceover.copy(currentPositionMs = 7000L)
    assertFalse(stateAfterVoiceover.isVoiceoverActive)
    assertEquals(0.8f, stateAfterVoiceover.getEffectiveTrackVolume(bgMusic), 0.001f)
  }

  @Test
  fun `audio ducking lowers background music volume when speech is detected on primary video track`() {
    val speechVideoClip = VideoClip(
      id = "video-speech-1",
      name = "Host Dialogue Clip",
      uri = "sample://video_speech",
      sourceDurationMs = 6000L,
      timelineStartMs = 1000L,
      trimInMs = 0L,
      trimOutMs = 6000L,
      volume = 1.0f,
      hasSpeech = true
    )
    val bgMusic = AudioTrackItem(
      id = "bg-synthwave-1",
      name = "Synthwave Beats",
      category = AudioCategory.MUSIC,
      timelineStartMs = 0L,
      durationMs = 12000L,
      volume = 1.0f
    )

    // Position at 500ms: Before video clip speech starts -> full volume 1.0
    val stateBeforeSpeech = com.example.viewmodel.EditorUiState(
      currentPositionMs = 500L,
      clips = listOf(speechVideoClip),
      audioClips = listOf(bgMusic),
      isAutoDuckingMasterEnabled = true,
      autoDuckingDuckLevel = 0.20f
    )
    assertFalse(stateBeforeSpeech.isPrimaryVideoSpeechActive)
    assertFalse(stateBeforeSpeech.isVoiceoverActive)
    assertEquals(1.0f, stateBeforeSpeech.getEffectiveTrackVolume(bgMusic), 0.001f)

    // Position at 2500ms: During primary video track speech -> BGM ducks to 1.0 * 0.20 = 0.20
    val stateDuringSpeech = stateBeforeSpeech.copy(currentPositionMs = 2500L)
    assertTrue(stateDuringSpeech.isPrimaryVideoSpeechActive)
    assertTrue(stateDuringSpeech.isVoiceoverActive)
    assertEquals(0.20f, stateDuringSpeech.getEffectiveTrackVolume(bgMusic), 0.001f)

    // If master auto-ducking is toggled off, volume remains untouched at 1.0 even during speech
    val stateDuckingDisabled = stateDuringSpeech.copy(isAutoDuckingMasterEnabled = false)
    assertFalse(stateDuckingDisabled.isVoiceoverActive)
    assertEquals(1.0f, stateDuckingDisabled.getEffectiveTrackVolume(bgMusic), 0.001f)
  }

  @Test
  fun `transitions system can be applied between clips on timeline`() {
    val clip1 = VideoClip(
      id = "clip-1",
      name = "Intro Scene",
      uri = "sample://video_1",
      sourceDurationMs = 5000L,
      timelineStartMs = 0L,
      transitionIn = TransitionType.NONE
    )
    val clip2 = VideoClip(
      id = "clip-2",
      name = "Action Scene",
      uri = "sample://video_2",
      sourceDurationMs = 5000L,
      timelineStartMs = 5000L,
      transitionIn = TransitionType.CROSSFADE,
      transitionDurationMs = 800L
    )
    val clip3 = VideoClip(
      id = "clip-3",
      name = "Outro Scene",
      uri = "sample://video_3",
      sourceDurationMs = 5000L,
      timelineStartMs = 10000L,
      transitionIn = TransitionType.WIPE_LEFT,
      transitionDurationMs = 600L
    )

    val state = com.example.viewmodel.EditorUiState(
      clips = listOf(clip1, clip2, clip3)
    )

    assertEquals(3, state.clips.size)
    assertEquals(TransitionType.NONE, state.clips[0].transitionIn)
    // Seam between Clip 1 and Clip 2: Crossfade dissolve
    assertEquals(TransitionType.CROSSFADE, state.clips[1].transitionIn)
    assertEquals(800L, state.clips[1].transitionDurationMs)
    // Seam between Clip 2 and Clip 3: Wipe left
    assertEquals(TransitionType.WIPE_LEFT, state.clips[2].transitionIn)
    assertEquals(600L, state.clips[2].transitionDurationMs)

    // Check common video effects exist in transition library
    assertTrue(TransitionType.entries.contains(TransitionType.CROSSFADE)) // Dissolve
    assertTrue(TransitionType.entries.contains(TransitionType.DIP_TO_BLACK)) // Fade
    assertTrue(TransitionType.entries.contains(TransitionType.DIP_TO_WHITE)) // Fade
    assertTrue(TransitionType.entries.contains(TransitionType.WIPE_LEFT)) // Wipe
    assertTrue(TransitionType.entries.contains(TransitionType.WIPE_RIGHT)) // Wipe
  }

  @Test
  fun `timeline serializer preserves hasSpeech attribute for primary video clips`() {
    val clipWithSpeech = VideoClip(
      id = "clip-speech-serialization-test",
      name = "Speech Clip",
      uri = "sample://video_speech_test",
      sourceDurationMs = 4000L,
      hasSpeech = true
    )
    val original = TimelineData(
      clips = listOf(clipWithSpeech),
      audioClips = emptyList(),
      textOverlays = emptyList(),
      stickers = emptyList()
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    assertTrue(restored.clips[0].hasSpeech)
  }

  @Test
  fun `timeline serializer round trip with professional color grading and LUT preset`() {
    val grading = com.example.data.model.ColorGradingSettings(
      exposure = 0.45f,
      temperature = 0.25f,
      tint = -0.15f,
      vibrance = 0.35f,
      lut = com.example.data.model.LutPreset.CYBERPUNK_NEON,
      liftR = 0.05f,
      liftG = -0.02f,
      liftB = 0.1f,
      gammaR = 0.12f,
      gammaG = 0.08f,
      gammaB = 0.04f,
      gainR = 0.95f,
      gainG = 1.05f,
      gainB = 1.2f,
      liftPedestal = -0.05f,
      gammaPedestal = 0.02f,
      gainPedestal = 0.15f
    )
    val clip = VideoClip(
      id = "clip-color-grading-test",
      name = "Cinematic Shot",
      uri = "sample://video_graded",
      sourceDurationMs = 5000L,
      colorGrading = grading
    )
    val original = TimelineData(
      clips = listOf(clip),
      audioClips = emptyList(),
      textOverlays = emptyList(),
      stickers = emptyList()
    )

    val json = TimelineSerializer.serialize(original)
    val restored = TimelineSerializer.deserialize(json)

    assertEquals(1, restored.clips.size)
    val restoredGrading = restored.clips[0].colorGrading
    assertEquals(0.45f, restoredGrading.exposure, 0.001f)
    assertEquals(0.25f, restoredGrading.temperature, 0.001f)
    assertEquals(-0.15f, restoredGrading.tint, 0.001f)
    assertEquals(0.35f, restoredGrading.vibrance, 0.001f)
    assertEquals(com.example.data.model.LutPreset.CYBERPUNK_NEON, restoredGrading.lut)
    assertEquals(0.05f, restoredGrading.liftR, 0.001f)
    assertEquals(0.12f, restoredGrading.gammaR, 0.001f)
    assertEquals(1.2f, restoredGrading.gainB, 0.001f)
    assertEquals(0.15f, restoredGrading.gainPedestal, 0.001f)
  }

  @Test
  fun `color grading viewModel updates and lut presets application`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Grade Test Clip", 5000L)
    }
    val clip = vm.uiState.value.clips.first()

    // 1. Set LUT Preset
    vm.setColorGradingLut(clip.id, com.example.data.model.LutPreset.TEAL_ORANGE)
    val afterLut = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(com.example.data.model.LutPreset.TEAL_ORANGE, afterLut.colorGrading.lut)

    // 2. Update Primary Settings
    val updatedSettings = afterLut.colorGrading.copy(exposure = 0.6f, temperature = -0.3f, vibrance = 0.5f)
    vm.updateColorGrading(clip.id, updatedSettings)
    val afterGrading = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(0.6f, afterGrading.colorGrading.exposure, 0.001f)
    assertEquals(-0.3f, afterGrading.colorGrading.temperature, 0.001f)
    assertEquals(0.5f, afterGrading.colorGrading.vibrance, 0.001f)

    // 3. Reset Grading
    vm.resetColorGrading(clip.id)
    val afterReset = vm.uiState.value.clips.find { it.id == clip.id }!!
    assertEquals(0f, afterReset.colorGrading.exposure, 0.001f)
    assertEquals(com.example.data.model.LutPreset.NONE, afterReset.colorGrading.lut)
  }

  @Test
  fun `davinci workspace switcher and sheet toggles`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    // Verify initial workspace
    assertEquals(com.example.data.model.EditorWorkspace.EDIT, vm.uiState.value.currentWorkspace)

    // Switch to Color Studio
    vm.setWorkspace(com.example.data.model.EditorWorkspace.COLOR)
    assertEquals(com.example.data.model.EditorWorkspace.COLOR, vm.uiState.value.currentWorkspace)

    // Switch to Fairlight Audio Mixing
    vm.setWorkspace(com.example.data.model.EditorWorkspace.AUDIO)
    assertEquals(com.example.data.model.EditorWorkspace.AUDIO, vm.uiState.value.currentWorkspace)

    // Switch to AI Studio
    vm.setWorkspace(com.example.data.model.EditorWorkspace.AI_MAGIC)
    assertEquals(com.example.data.model.EditorWorkspace.AI_MAGIC, vm.uiState.value.currentWorkspace)

    // Toggle Color Grading Sheet & AI Sheet
    vm.toggleColorGradingSheet(true)
    assertTrue(vm.uiState.value.showColorGradingSheet)
    vm.toggleColorGradingSheet(false)
    assertFalse(vm.uiState.value.showColorGradingSheet)

    vm.toggleAiStudioSheet(true)
    assertTrue(vm.uiState.value.showAiStudioSheet)
    vm.toggleAiStudioSheet(false)
    assertFalse(vm.uiState.value.showAiStudioSheet)

    // Toggle Before/After Split Comparison
    assertFalse(vm.uiState.value.isColorCompareSplit)
    vm.toggleColorCompareSplit()
    assertTrue(vm.uiState.value.isColorCompareSplit)
  }

  @Test
  fun `ai studio silence removal ripples timeline and updates duration`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Silence Test Clip", 8000L)
    }
    val clip = vm.uiState.value.clips.first()
    val initialDuration = clip.trimmedDurationMs

    // Execute Silence Removal with -35 dB threshold
    vm.executeAiSilenceRemoval(clip.id, -35f)

    val newClips = vm.uiState.value.clips
    assertTrue(newClips.size >= 2)
    val totalNewDuration = newClips.sumOf { it.trimmedDurationMs }
    assertTrue("Trimmed duration should be reduced after removing silent sections", totalNewDuration < initialDuration)
    assertNotNull(vm.uiState.value.statusMessage)
    assertTrue(vm.uiState.value.statusMessage!!.contains("silence removed"))
  }

  @Test
  fun `ai studio auto captions creates synchronized subtitle overlays`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Podcast Dialogue", 10000L)
    }

    val initialCaptionCount = vm.uiState.value.textOverlays.size
    vm.generateAiAutoCaptions(com.example.data.model.TextStylePreset.NEON_GLOW)

    val finalCaptions = vm.uiState.value.textOverlays
    assertTrue(finalCaptions.size > initialCaptionCount)
    // Check that captions have styling preset applied
    assertTrue(finalCaptions.any { it.preset == com.example.data.model.TextStylePreset.NEON_GLOW })
    assertNotNull(vm.uiState.value.statusMessage)
    assertTrue(vm.uiState.value.statusMessage!!.contains("Subtitles generated"))
  }

  @Test
  fun `ai studio smart reframing updates project aspect ratio`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)

    vm.applyAiSmartReframe(com.example.data.model.AspectRatio.VERTICAL_9_16)
    assertEquals(com.example.data.model.AspectRatio.VERTICAL_9_16, vm.uiState.value.aspectRatio)

    vm.applyAiSmartReframe(com.example.data.model.AspectRatio.SQUARE_1_1)
    assertEquals(com.example.data.model.AspectRatio.SQUARE_1_1, vm.uiState.value.aspectRatio)
  }

  @Test
  fun `ai studio scene cut detection adds timeline cue markers`() {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    if (vm.uiState.value.clips.isEmpty()) {
      vm.addCustomMediaClip("sample://1", "Action Sequence", 12000L)
    }
    val clip = vm.uiState.value.clips.first()

    val initialMarkerCount = vm.uiState.value.timelineMarkers.size
    vm.detectAiSceneCuts(clip.id)

    val newMarkerCount = vm.uiState.value.timelineMarkers.size
    assertTrue("Scene detection should add cut markers on the timeline", newMarkerCount > initialMarkerCount)
    assertNotNull(vm.uiState.value.statusMessage)
    assertTrue(vm.uiState.value.statusMessage!!.contains("scene cut detected"))
  }

  @Test
  fun `room database schema includes projects, layers, and edit history tables`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, OpenCutDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val projectDao = db.projectDao()
    val layerDao = db.projectLayerDao()
    val historyDao = db.projectEditHistoryDao()

    // 1. Insert Project
    val proj = ProjectEntity(
      id = "test-proj-schema-1",
      title = "Cinema Project",
      aspectRatioName = "HORIZONTAL_16_9",
      resolutionName = "RES_1080P",
      fps = 30,
      durationMs = 15000L,
      clipCount = 2,
      updatedAt = System.currentTimeMillis(),
      thumbnailGradientStart = 0xFF1E1B4B,
      thumbnailGradientEnd = 0xFF00E5FF,
      timelineDataJson = "{}",
      createdAt = System.currentTimeMillis(),
      lastPlayheadPositionMs = 4500L,
      currentWorkspace = "COLOR"
    )
    projectDao.insertProject(proj)
    val fetchedProj = projectDao.getProjectById(proj.id)
    assertNotNull(fetchedProj)
    assertEquals("Cinema Project", fetchedProj?.title)
    assertEquals(4500L, fetchedProj?.lastPlayheadPositionMs)
    assertEquals("COLOR", fetchedProj?.currentWorkspace)

    // 2. Insert Layer Metadata
    val layerVideo = com.example.data.db.ProjectLayerEntity(
      layerId = "layer-v1",
      projectId = proj.id,
      layerIndex = 0,
      layerType = "VIDEO",
      layerName = "Main Video Track",
      isVisible = true,
      isLocked = false,
      isMuted = false,
      itemCount = 2
    )
    val layerAudio = com.example.data.db.ProjectLayerEntity(
      layerId = "layer-a1",
      projectId = proj.id,
      layerIndex = 1,
      layerType = "AUDIO",
      layerName = "Background Music",
      isVisible = true,
      isLocked = false,
      isMuted = true,
      volume = 0.8f,
      itemCount = 1
    )
    layerDao.insertLayers(listOf(layerVideo, layerAudio))
    val layers = layerDao.getLayersForProjectSync(proj.id)
    assertEquals(2, layers.size)
    assertEquals("VIDEO", layers[0].layerType)
    assertEquals(false, layers[0].isMuted)
    assertEquals("AUDIO", layers[1].layerType)
    assertEquals(true, layers[1].isMuted)

    // 3. Insert Edit History
    val history1 = com.example.data.db.ProjectEditHistoryEntity(
      historyId = "hist-1",
      projectId = proj.id,
      stepIndex = 0,
      actionType = "PROJECT_CREATED",
      actionDescription = "Created project",
      timestamp = System.currentTimeMillis(),
      timelineSnapshotJson = "{}",
      playheadPositionMs = 0L,
      isCurrentStep = false
    )
    val history2 = com.example.data.db.ProjectEditHistoryEntity(
      historyId = "hist-2",
      projectId = proj.id,
      stepIndex = 1,
      actionType = "APPLY_COLOR_GRADE",
      actionDescription = "Graded with Cyberpunk Neon LUT",
      timestamp = System.currentTimeMillis() + 1000L,
      timelineSnapshotJson = "{}",
      playheadPositionMs = 4500L,
      isCurrentStep = true
    )
    historyDao.insertHistoryEntries(listOf(history1, history2))
    val historyList = historyDao.getHistoryListForProject(proj.id)
    assertEquals(2, historyList.size)
    val currentStep = historyDao.getCurrentStepForProject(proj.id)
    assertNotNull(currentStep)
    assertEquals("APPLY_COLOR_GRADE", currentStep?.actionType)
    assertEquals("hist-2", currentStep?.historyId)

    db.close()
  }

  @Test
  fun `project layer dao updates visibility lock and mute states`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, OpenCutDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val projectDao = db.projectDao()
    val layerDao = db.projectLayerDao()

    val proj = ProjectEntity(
      id = "proj-layer-test",
      title = "Layer Test",
      aspectRatioName = "VERTICAL_9_16",
      resolutionName = "RES_1080P",
      fps = 30,
      durationMs = 8000L,
      clipCount = 1,
      updatedAt = System.currentTimeMillis(),
      thumbnailGradientStart = 0L,
      thumbnailGradientEnd = 0L,
      timelineDataJson = "{}"
    )
    projectDao.insertProject(proj)

    val layer = com.example.data.db.ProjectLayerEntity(
      layerId = "layer-text-1",
      projectId = proj.id,
      layerIndex = 2,
      layerType = "TEXT",
      layerName = "Subtitles",
      isVisible = true,
      isLocked = false,
      isMuted = false
    )
    layerDao.insertLayer(layer)

    // Toggle visibility to false
    layerDao.setLayerVisibility(layer.layerId, false)
    val fetchedAfterVis = layerDao.getLayerById(layer.layerId)
    assertFalse(fetchedAfterVis!!.isVisible)

    // Toggle lock to true
    layerDao.setLayerLock(layer.layerId, true)
    val fetchedAfterLock = layerDao.getLayerById(layer.layerId)
    assertTrue(fetchedAfterLock!!.isLocked)

    // Toggle mute to true
    layerDao.setLayerMute(layer.layerId, true)
    val fetchedAfterMute = layerDao.getLayerById(layer.layerId)
    assertTrue(fetchedAfterMute!!.isMuted)

    db.close()
  }

  @Test
  fun `cascade delete removes layers and editing history when project is deleted`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, OpenCutDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val projectDao = db.projectDao()
    val layerDao = db.projectLayerDao()
    val historyDao = db.projectEditHistoryDao()

    val proj = ProjectEntity(
      id = "cascade-proj",
      title = "To Be Deleted",
      aspectRatioName = "VERTICAL_9_16",
      resolutionName = "RES_1080P",
      fps = 30,
      durationMs = 5000L,
      clipCount = 1,
      updatedAt = System.currentTimeMillis(),
      thumbnailGradientStart = 0L,
      thumbnailGradientEnd = 0L,
      timelineDataJson = "{}"
    )
    projectDao.insertProject(proj)

    val layer = com.example.data.db.ProjectLayerEntity(
      layerId = "cascade-layer",
      projectId = proj.id,
      layerIndex = 0,
      layerType = "VIDEO",
      layerName = "Video Track"
    )
    layerDao.insertLayer(layer)

    val history = com.example.data.db.ProjectEditHistoryEntity(
      historyId = "cascade-hist",
      projectId = proj.id,
      stepIndex = 0,
      actionType = "PROJECT_CREATED",
      actionDescription = "Created",
      timelineSnapshotJson = "{}"
    )
    historyDao.insertHistoryEntry(history)

    assertEquals(1, layerDao.getLayersForProjectSync(proj.id).size)
    assertEquals(1, historyDao.getHistoryListForProject(proj.id).size)

    // Delete project
    projectDao.deleteProject(proj)

    // Verify cascade deletion
    assertEquals(0, layerDao.getLayersForProjectSync(proj.id).size)
    assertEquals(0, historyDao.getHistoryListForProject(proj.id).size)

    db.close()
  }

  @Test
  fun `resuming work restores playhead position, workspace, layers, and history`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val db = OpenCutDatabase.getDatabase(context)
    val repo = com.example.data.repository.ProjectRepository(
      projectDao = db.projectDao(),
      importedMediaDao = db.importedMediaDao(),
      projectLayerDao = db.projectLayerDao(),
      projectEditHistoryDao = db.projectEditHistoryDao()
    )

    // 1. Create project with sample content
    val proj = repo.createNewProject(
      title = "Resume Test Project",
      aspectRatio = com.example.data.model.AspectRatio.VERTICAL_9_16,
      withSampleContent = true
    )
    assertNotNull(proj)
    val projId = proj.id

    // Check that layers and initial history step were inserted
    val layers = repo.getLayersForProjectSync(projId)
    assertTrue("Project layers should be created in Room", layers.isNotEmpty())
    val history = repo.getEditHistoryList(projId)
    assertTrue("Project edit history should have initial creation step", history.isNotEmpty())

    // 2. Update project with resumed playhead and workspace
    val updatedProj = proj.copy(
      lastPlayheadPositionMs = 3200L,
      currentWorkspace = "COLOR"
    )
    repo.saveProject(updatedProj)

    // 3. Open project with ViewModel and verify state restoration
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.openProjectSync(projId)

    val resumedState = vm.uiState.value
    assertEquals(projId, resumedState.currentProject?.id)
    assertEquals(com.example.data.model.EditorWorkspace.COLOR, resumedState.currentWorkspace)
    assertEquals(3200L, resumedState.currentPositionMs)
    assertEquals(layers.size, resumedState.projectLayers.size)
    assertTrue("Status message should indicate resumption", resumedState.statusMessage!!.contains("Resumed"))
  }

  @Test
  fun `motion vfx studio applies speed ramp, particle vfx, and motion tracking`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val db = OpenCutDatabase.getDatabase(context)
    val repo = com.example.data.repository.ProjectRepository(
      projectDao = db.projectDao(),
      importedMediaDao = db.importedMediaDao(),
      projectLayerDao = db.projectLayerDao(),
      projectEditHistoryDao = db.projectEditHistoryDao()
    )
    val proj = repo.createNewProject("Motion VFX Test", withSampleContent = true)
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.openProjectSync(proj.id)

    vm.setWorkspace(com.example.data.model.EditorWorkspace.MOTION_VFX)
    assertEquals(com.example.data.model.EditorWorkspace.MOTION_VFX, vm.uiState.value.currentWorkspace)

    // Apply Bullet Time speed ramp with Optical Flow
    vm.applySpeedRampPreset(
      com.example.data.model.SpeedRampPreset.BULLET_TIME,
      com.example.data.model.FrameInterpolationMode.OPTICAL_FLOW,
      true
    )
    assertEquals(0.25f, vm.uiState.value.clips.first().speed, 0.01f)

    // Apply Particle VFX overlay
    val initialStickers = vm.uiState.value.stickers.size
    vm.applyParticleVfx(com.example.data.model.VfxParticlePreset.NEON_SPARKS)
    assertEquals(initialStickers + 1, vm.uiState.value.stickers.size)

    // Apply AI Motion Tracker
    val initialTexts = vm.uiState.value.textOverlays.size
    vm.applyMotionTracker("AI Face & Subject Lock", "Attach Callout Text")
    assertEquals(initialTexts + 1, vm.uiState.value.textOverlays.size)
    assertTrue(vm.uiState.value.clips.first().hasKeyframe)
  }

  @Test
  fun `ai copilot and smart proxy mode update timeline and performance state`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val db = OpenCutDatabase.getDatabase(context)
    val repo = com.example.data.repository.ProjectRepository(
      projectDao = db.projectDao(),
      importedMediaDao = db.importedMediaDao(),
      projectLayerDao = db.projectLayerDao(),
      projectEditHistoryDao = db.projectEditHistoryDao()
    )
    val proj = repo.createNewProject("AI Copilot Test", withSampleContent = true)
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    vm.openProjectSync(proj.id)

    // Cycle smart proxy quality mode
    assertEquals(com.example.data.model.ProxyQualityMode.FULL, vm.uiState.value.proxyQualityMode)
    vm.cycleProxyQualityMode()
    assertEquals(com.example.data.model.ProxyQualityMode.HALF_PROXY, vm.uiState.value.proxyQualityMode)

    // Execute AI Copilot natural language prompt
    vm.executeAiCopilotPrompt("Fast TikTok viral cut with neon captions, beat sync & teal-orange LUT")
    val state = vm.uiState.value
    assertEquals(com.example.data.model.AspectRatio.VERTICAL_9_16, state.aspectRatio)
    assertEquals(com.example.data.model.LutPreset.TEAL_ORANGE, state.clips.first().colorGrading.lut)
    assertTrue(state.textOverlays.any { it.text.contains("VIRAL") })
  }

  @Test
  fun `device media importer generates test video file on storage`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val file = com.example.util.DeviceMediaImporter.createDeviceTestVideo(context, "test_camera_sample.mp4")
    assertTrue(file.exists())
    assertTrue(file.length() > 0)
    assertEquals("test_camera_sample.mp4", file.name)

    val metadata = com.example.util.DeviceMediaImporter.extractMetadata(context, file.absolutePath)
    assertTrue(metadata.durationMs > 0L)
    assertTrue(metadata.width > 0)
    assertTrue(metadata.height > 0)
  }

  @Test
  fun `video editor viewModel imports custom media clip smoothly`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>() as android.app.Application
    val vm = com.example.viewmodel.VideoEditorViewModel(context)
    val initialCount = vm.uiState.value.clips.size

    vm.addCustomMediaClip("file:///sdcard/Movies/MyVacation.mp4", "MyVacation.mp4", 8000L)
    // Yield to allow coroutine to complete
    kotlinx.coroutines.delay(100)

    val updatedClips = vm.uiState.value.clips
    assertTrue(updatedClips.size >= initialCount + 1)
    val importedClip = updatedClips.last()
    assertEquals("MyVacation.mp4", importedClip.name)
    assertEquals(8000L, importedClip.sourceDurationMs)
    assertEquals(8000L, importedClip.trimmedDurationMs)
  }
}




