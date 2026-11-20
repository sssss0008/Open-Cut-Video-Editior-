package com.example.engine

import com.example.data.model.ImageOverlay
import com.example.data.model.KeyframeCurve
import com.example.data.model.OpenShotKeyframe
import com.example.data.model.VideoClip
import java.util.UUID
import kotlin.math.abs

data class ClipTransformState(
    val scale: Float,
    val rotation: Float,
    val alpha: Float,
    val positionX: Float,
    val positionY: Float
)

/**
 * Editing engine for evaluating, inserting, updating, clamping, and generating
 * time-based keyframe animations for clip opacity, scale, 2D position, and rotation.
 */
object KeyframeEngine {
    const val KEYFRAME_SNAP_THRESHOLD_MS = 120L

    fun applyCurve(rawT: Float, curve: KeyframeCurve): Float {
        val t = rawT.coerceIn(0f, 1f)
        return when (curve) {
            KeyframeCurve.LINEAR -> t
            KeyframeCurve.CONSTANT -> 0f
            KeyframeCurve.BEZIER -> t * t * (3f - 2f * t)
            KeyframeCurve.EASE_IN -> t * t
            KeyframeCurve.EASE_OUT -> t * (2f - t)
        }
    }

    fun interpolate(
        keyframes: List<OpenShotKeyframe>,
        elapsedInClipMs: Long,
        defaultScale: Float = 1.0f,
        defaultRotation: Float = 0f,
        defaultAlpha: Float = 1.0f,
        defaultPosX: Float = 0f,
        defaultPosY: Float = 0f
    ): ClipTransformState {
        if (keyframes.isEmpty()) {
            return ClipTransformState(
                scale = defaultScale.coerceIn(0.1f, 5.0f),
                rotation = defaultRotation,
                alpha = defaultAlpha.coerceIn(0f, 1f),
                positionX = defaultPosX.coerceIn(-1.5f, 1.5f),
                positionY = defaultPosY.coerceIn(-1.5f, 1.5f)
            )
        }

        val sorted = keyframes.sortedBy { it.timeOffsetMs }
        if (elapsedInClipMs <= sorted.first().timeOffsetMs) {
            val first = sorted.first()
            return ClipTransformState(
                scale = first.scale.coerceIn(0.1f, 5.0f),
                rotation = first.rotation,
                alpha = first.alpha.coerceIn(0f, 1f),
                positionX = first.positionX.coerceIn(-1.5f, 1.5f),
                positionY = first.positionY.coerceIn(-1.5f, 1.5f)
            )
        }
        if (elapsedInClipMs >= sorted.last().timeOffsetMs) {
            val last = sorted.last()
            return ClipTransformState(
                scale = last.scale.coerceIn(0.1f, 5.0f),
                rotation = last.rotation,
                alpha = last.alpha.coerceIn(0f, 1f),
                positionX = last.positionX.coerceIn(-1.5f, 1.5f),
                positionY = last.positionY.coerceIn(-1.5f, 1.5f)
            )
        }

        for (i in 0 until sorted.size - 1) {
            val k1 = sorted[i]
            val k2 = sorted[i + 1]
            if (elapsedInClipMs in k1.timeOffsetMs..k2.timeOffsetMs) {
                val span = (k2.timeOffsetMs - k1.timeOffsetMs).coerceAtLeast(1L).toFloat()
                val rawT = ((elapsedInClipMs - k1.timeOffsetMs) / span).coerceIn(0f, 1f)
                val t = applyCurve(rawT, k1.curve)
                return ClipTransformState(
                    scale = (k1.scale + (k2.scale - k1.scale) * t).coerceIn(0.1f, 5.0f),
                    rotation = k1.rotation + (k2.rotation - k1.rotation) * t,
                    alpha = (k1.alpha + (k2.alpha - k1.alpha) * t).coerceIn(0f, 1f),
                    positionX = (k1.positionX + (k2.positionX - k1.positionX) * t).coerceIn(-1.5f, 1.5f),
                    positionY = (k1.positionY + (k2.positionY - k1.positionY) * t).coerceIn(-1.5f, 1.5f)
                )
            }
        }

        return ClipTransformState(
            scale = defaultScale.coerceIn(0.1f, 5.0f),
            rotation = defaultRotation,
            alpha = defaultAlpha.coerceIn(0f, 1f),
            positionX = defaultPosX.coerceIn(-1.5f, 1.5f),
            positionY = defaultPosY.coerceIn(-1.5f, 1.5f)
        )
    }

    fun evaluateClipAtElapsedMs(clip: VideoClip, elapsedInClipMs: Long): ClipTransformState {
        val clampedElapsed = elapsedInClipMs.coerceIn(0L, clip.trimmedDurationMs.coerceAtLeast(1L))
        return interpolate(
            keyframes = clip.keyframes,
            elapsedInClipMs = clampedElapsed,
            defaultScale = clip.scale,
            defaultRotation = clip.rotation,
            defaultAlpha = clip.alpha,
            defaultPosX = clip.positionX,
            defaultPosY = clip.positionY
        )
    }

    fun evaluateClipAtTimelineMs(clip: VideoClip, timelinePositionMs: Long): ClipTransformState {
        val elapsed = (timelinePositionMs - clip.timelineStartMs).coerceAtLeast(0L)
        return evaluateClipAtElapsedMs(clip, elapsed)
    }

    fun upsertKeyframeAtOffset(
        clip: VideoClip,
        timeOffsetMs: Long,
        scale: Float? = null,
        alpha: Float? = null,
        rotation: Float? = null,
        positionX: Float? = null,
        positionY: Float? = null,
        curve: KeyframeCurve? = null,
        snapThresholdMs: Long = KEYFRAME_SNAP_THRESHOLD_MS
    ): Pair<VideoClip, OpenShotKeyframe> {
        val duration = clip.trimmedDurationMs.coerceAtLeast(100L)
        val clampedOffset = timeOffsetMs.coerceIn(0L, duration)
        val existing = clip.keyframes.minByOrNull { abs(it.timeOffsetMs - clampedOffset) }
            ?.takeIf { abs(it.timeOffsetMs - clampedOffset) <= snapThresholdMs }

        return if (existing != null) {
            val updatedKf = existing.copy(
                scale = (scale ?: existing.scale).coerceIn(0.1f, 5.0f),
                alpha = (alpha ?: existing.alpha).coerceIn(0f, 1f),
                rotation = rotation ?: existing.rotation,
                positionX = (positionX ?: existing.positionX).coerceIn(-1.0f, 1.0f),
                positionY = (positionY ?: existing.positionY).coerceIn(-1.0f, 1.0f),
                curve = curve ?: existing.curve
            )
            val updatedList = clip.keyframes.map { if (it.id == existing.id) updatedKf else it }
                .sortedBy { it.timeOffsetMs }
            clip.copy(keyframes = updatedList, hasKeyframe = updatedList.isNotEmpty()) to updatedKf
        } else {
            val baseState = evaluateClipAtElapsedMs(clip, clampedOffset)
            val newKf = OpenShotKeyframe(
                id = UUID.randomUUID().toString(),
                timeOffsetMs = clampedOffset,
                scale = (scale ?: baseState.scale).coerceIn(0.1f, 5.0f),
                alpha = (alpha ?: baseState.alpha).coerceIn(0f, 1f),
                rotation = rotation ?: baseState.rotation,
                positionX = (positionX ?: baseState.positionX).coerceIn(-1.0f, 1.0f),
                positionY = (positionY ?: baseState.positionY).coerceIn(-1.0f, 1.0f),
                curve = curve ?: KeyframeCurve.BEZIER
            )
            val updatedList = (clip.keyframes + newKf).sortedBy { it.timeOffsetMs }
            clip.copy(keyframes = updatedList, hasKeyframe = true) to newKf
        }
    }

    fun evaluateImageOverlayAtElapsedMs(overlay: ImageOverlay, elapsedInOverlayMs: Long): ClipTransformState {
        val clampedElapsed = elapsedInOverlayMs.coerceIn(0L, overlay.durationMs.coerceAtLeast(1L))
        return interpolate(
            keyframes = overlay.keyframes,
            elapsedInClipMs = clampedElapsed,
            defaultScale = overlay.scale,
            defaultRotation = overlay.rotation,
            defaultAlpha = overlay.alpha,
            defaultPosX = overlay.positionX,
            defaultPosY = overlay.positionY
        )
    }

    fun evaluateImageOverlayAtTimelineMs(overlay: ImageOverlay, timelinePositionMs: Long): ClipTransformState {
        val elapsed = (timelinePositionMs - overlay.timelineStartMs).coerceAtLeast(0L)
        return evaluateImageOverlayAtElapsedMs(overlay, elapsed)
    }

    fun upsertKeyframeAtOffset(
        overlay: ImageOverlay,
        timeOffsetMs: Long,
        scale: Float? = null,
        alpha: Float? = null,
        rotation: Float? = null,
        positionX: Float? = null,
        positionY: Float? = null,
        curve: KeyframeCurve? = null,
        snapThresholdMs: Long = KEYFRAME_SNAP_THRESHOLD_MS
    ): Pair<ImageOverlay, OpenShotKeyframe> {
        val duration = overlay.durationMs.coerceAtLeast(100L)
        val clampedOffset = timeOffsetMs.coerceIn(0L, duration)
        val existing = overlay.keyframes.minByOrNull { abs(it.timeOffsetMs - clampedOffset) }
            ?.takeIf { abs(it.timeOffsetMs - clampedOffset) <= snapThresholdMs }

        return if (existing != null) {
            val updatedKf = existing.copy(
                scale = (scale ?: existing.scale).coerceIn(0.1f, 5.0f),
                alpha = (alpha ?: existing.alpha).coerceIn(0f, 1f),
                rotation = rotation ?: existing.rotation,
                positionX = (positionX ?: existing.positionX).coerceIn(-1.5f, 1.5f),
                positionY = (positionY ?: existing.positionY).coerceIn(-1.5f, 1.5f),
                curve = curve ?: existing.curve
            )
            val updatedList = overlay.keyframes.map { if (it.id == existing.id) updatedKf else it }
                .sortedBy { it.timeOffsetMs }
            overlay.copy(keyframes = updatedList, hasKeyframe = updatedList.isNotEmpty()) to updatedKf
        } else {
            val baseState = evaluateImageOverlayAtElapsedMs(overlay, clampedOffset)
            val newKf = OpenShotKeyframe(
                id = UUID.randomUUID().toString(),
                timeOffsetMs = clampedOffset,
                scale = (scale ?: baseState.scale).coerceIn(0.1f, 5.0f),
                alpha = (alpha ?: baseState.alpha).coerceIn(0f, 1f),
                rotation = rotation ?: baseState.rotation,
                positionX = (positionX ?: baseState.positionX).coerceIn(-1.5f, 1.5f),
                positionY = (positionY ?: baseState.positionY).coerceIn(-1.5f, 1.5f),
                curve = curve ?: KeyframeCurve.BEZIER
            )
            val updatedList = (overlay.keyframes + newKf).sortedBy { it.timeOffsetMs }
            overlay.copy(keyframes = updatedList, hasKeyframe = true) to newKf
        }
    }

    fun removeKeyframe(overlay: ImageOverlay, keyframeId: String): ImageOverlay {
        val updated = overlay.keyframes.filter { it.id != keyframeId }.sortedBy { it.timeOffsetMs }
        return overlay.copy(keyframes = updated, hasKeyframe = updated.isNotEmpty())
    }

    fun removeKeyframe(clip: VideoClip, keyframeId: String): VideoClip {
        val updated = clip.keyframes.filter { it.id != keyframeId }.sortedBy { it.timeOffsetMs }
        return clip.copy(keyframes = updated, hasKeyframe = updated.isNotEmpty())
    }

    fun clampKeyframesToDuration(clip: VideoClip): VideoClip {
        if (clip.keyframes.isEmpty()) return clip
        val maxDur = clip.trimmedDurationMs.coerceAtLeast(100L)
        val clamped = clip.keyframes
            .map { it.copy(timeOffsetMs = it.timeOffsetMs.coerceIn(0L, maxDur)) }
            .sortedBy { it.timeOffsetMs }
            .distinctBy { it.timeOffsetMs }
        return clip.copy(keyframes = clamped, hasKeyframe = clamped.isNotEmpty())
    }

    fun createPresetKeyframes(presetName: String, clipDurationMs: Long): List<OpenShotKeyframe> {
        val dur = clipDurationMs.coerceAtLeast(400L)
        return when (presetName) {
            "KEN_BURNS_ZOOM" -> listOf(
                OpenShotKeyframe(timeOffsetMs = 0L, scale = 1.0f, curve = KeyframeCurve.BEZIER),
                OpenShotKeyframe(timeOffsetMs = dur, scale = 1.35f, curve = KeyframeCurve.BEZIER)
            )
            "PAN_LEFT_RIGHT" -> listOf(
                OpenShotKeyframe(timeOffsetMs = 0L, positionX = -0.35f, scale = 1.15f, curve = KeyframeCurve.BEZIER),
                OpenShotKeyframe(timeOffsetMs = dur, positionX = 0.35f, scale = 1.15f, curve = KeyframeCurve.BEZIER)
            )
            "FADE_DISSOLVE" -> {
                val fadeWindow = (dur / 4L).coerceIn(150L, 800L)
                listOf(
                    OpenShotKeyframe(timeOffsetMs = 0L, alpha = 0.0f, curve = KeyframeCurve.LINEAR),
                    OpenShotKeyframe(timeOffsetMs = fadeWindow, alpha = 1.0f, curve = KeyframeCurve.LINEAR),
                    OpenShotKeyframe(timeOffsetMs = (dur - fadeWindow).coerceAtLeast(fadeWindow + 100L), alpha = 1.0f, curve = KeyframeCurve.LINEAR),
                    OpenShotKeyframe(timeOffsetMs = dur, alpha = 0.0f, curve = KeyframeCurve.LINEAR)
                )
            }
            "SLIDE_AND_SCALE" -> listOf(
                OpenShotKeyframe(timeOffsetMs = 0L, scale = 0.8f, alpha = 0.0f, positionX = -0.5f, positionY = 0.15f, curve = KeyframeCurve.EASE_OUT),
                OpenShotKeyframe(timeOffsetMs = dur / 2L, scale = 1.1f, alpha = 1.0f, positionX = 0f, positionY = 0f, curve = KeyframeCurve.BEZIER),
                OpenShotKeyframe(timeOffsetMs = dur, scale = 1.0f, alpha = 1.0f, positionX = 0.25f, positionY = -0.1f, curve = KeyframeCurve.EASE_IN)
            )
            "SPIN_360" -> listOf(
                OpenShotKeyframe(timeOffsetMs = 0L, rotation = 0f, scale = 0.8f, curve = KeyframeCurve.BEZIER),
                OpenShotKeyframe(timeOffsetMs = dur / 2L, rotation = 180f, scale = 1.15f, curve = KeyframeCurve.BEZIER),
                OpenShotKeyframe(timeOffsetMs = dur, rotation = 360f, scale = 1.0f, curve = KeyframeCurve.BEZIER)
            )
            else -> listOf(
                OpenShotKeyframe(timeOffsetMs = 0L, scale = 1.0f, curve = KeyframeCurve.BEZIER),
                OpenShotKeyframe(timeOffsetMs = dur, scale = 1.25f, curve = KeyframeCurve.BEZIER)
            )
        }
    }
}
