package com.example.engine

import com.example.data.model.TransitionType
import com.example.data.model.VideoClip

data class TransitionSeam(
    val index: Int,
    val outgoingClip: VideoClip?,
    val incomingClip: VideoClip,
    val seamTimeMs: Long,
    val transitionType: TransitionType,
    val durationMs: Long,
    val maxDurationMs: Long
) {
    val isCutBetweenAdjacentClips: Boolean
        get() = outgoingClip != null

    val hasActiveTransition: Boolean
        get() = transitionType != TransitionType.NONE && durationMs > 0L

    val shortLabel: String
        get() = if (outgoingClip != null) {
            "Cut $index: ${outgoingClip.name.take(8)} ➔ ${incomingClip.name.take(8)}"
        } else {
            "Intro ➔ ${incomingClip.name.take(10)}"
        }
}

data class ActiveTransitionState(
    val outgoingClip: VideoClip?,
    val incomingClip: VideoClip,
    val transitionType: TransitionType,
    val durationMs: Long,
    val rawProgress: Float,
    val easedProgress: Float
)

/**
 * Engine coordinating transitions between adjacent timeline video clips:
 * seam discovery, safe duration clamping based on adjacent clip lengths,
 * batch application/removal, and playhead transition evaluation.
 */
object TransitionsManager {
    const val MIN_TRANSITION_DURATION_MS = 200L
    const val MAX_TRANSITION_DURATION_MS = 2500L
    const val DEFAULT_TRANSITION_DURATION_MS = 600L

    fun maxSafeDurationMs(outgoingClip: VideoClip?, incomingClip: VideoClip): Long {
        val incomingDur = incomingClip.trimmedDurationMs.coerceAtLeast(MIN_TRANSITION_DURATION_MS)
        val limitingDur = if (outgoingClip != null) {
            minOf(outgoingClip.trimmedDurationMs.coerceAtLeast(MIN_TRANSITION_DURATION_MS), incomingDur)
        } else {
            incomingDur
        }
        val proportionalCap = (limitingDur * 0.75f).toLong().coerceAtLeast(MIN_TRANSITION_DURATION_MS)
        return minOf(MAX_TRANSITION_DURATION_MS, proportionalCap)
    }

    fun clampDuration(
        requestedDurationMs: Long,
        outgoingClip: VideoClip?,
        incomingClip: VideoClip,
        transition: TransitionType
    ): Long {
        if (transition == TransitionType.NONE) return 0L
        val maxSafe = maxSafeDurationMs(outgoingClip, incomingClip)
        return requestedDurationMs.coerceIn(MIN_TRANSITION_DURATION_MS, maxSafe)
    }

    fun discoverSeams(clips: List<VideoClip>): List<TransitionSeam> {
        if (clips.isEmpty()) return emptyList()
        return clips.mapIndexed { index, incoming ->
            val outgoing = if (index > 0) clips[index - 1] else null
            val maxSafe = maxSafeDurationMs(outgoing, incoming)
            TransitionSeam(
                index = index,
                outgoingClip = outgoing,
                incomingClip = incoming,
                seamTimeMs = incoming.timelineStartMs,
                transitionType = incoming.transitionIn,
                durationMs = if (incoming.transitionIn == TransitionType.NONE) 0L else incoming.transitionDurationMs.coerceIn(MIN_TRANSITION_DURATION_MS, maxSafe),
                maxDurationMs = maxSafe
            )
        }
    }

    fun discoverCutSeams(clips: List<VideoClip>): List<TransitionSeam> {
        return discoverSeams(clips).filter { it.isCutBetweenAdjacentClips }
    }

    fun findOutgoingClip(clips: List<VideoClip>, incomingClip: VideoClip?): VideoClip? {
        if (incomingClip == null) return null
        val index = clips.indexOfFirst { it.id == incomingClip.id }
        return if (index > 0) clips[index - 1] else null
    }

    fun applyTransitionToSeam(
        clips: List<VideoClip>,
        incomingClipId: String,
        transition: TransitionType,
        durationMs: Long = DEFAULT_TRANSITION_DURATION_MS
    ): List<VideoClip> {
        return clips.mapIndexed { index, clip ->
            if (clip.id != incomingClipId) {
                clip
            } else {
                val outgoing = if (index > 0) clips[index - 1] else null
                val clamped = clampDuration(durationMs, outgoing, clip, transition)
                clip.copy(
                    transitionIn = transition,
                    transitionDurationMs = clamped
                )
            }
        }
    }

    fun applyTransitionToAllCuts(
        clips: List<VideoClip>,
        transition: TransitionType,
        durationMs: Long = DEFAULT_TRANSITION_DURATION_MS,
        includeIntro: Boolean = true
    ): List<VideoClip> {
        return clips.mapIndexed { index, clip ->
            if (!includeIntro && index == 0) {
                clip
            } else {
                val outgoing = if (index > 0) clips[index - 1] else null
                val clamped = clampDuration(durationMs, outgoing, clip, transition)
                clip.copy(
                    transitionIn = transition,
                    transitionDurationMs = clamped
                )
            }
        }
    }

    fun removeTransition(
        clips: List<VideoClip>,
        incomingClipId: String
    ): List<VideoClip> {
        return applyTransitionToSeam(
            clips = clips,
            incomingClipId = incomingClipId,
            transition = TransitionType.NONE,
            durationMs = 0L
        )
    }

    fun evaluateTransitionAtTimelineMs(
        clips: List<VideoClip>,
        timelinePositionMs: Long
    ): ActiveTransitionState? {
        if (clips.isEmpty()) return null
        val index = clips.indexOfFirst {
            timelinePositionMs >= it.timelineStartMs && timelinePositionMs < it.timelineEndMs
        }
        if (index < 0) return null
        val incoming = clips[index]
        if (incoming.transitionIn == TransitionType.NONE || incoming.transitionDurationMs <= 0L) {
            return null
        }

        val outgoing = if (index > 0) clips[index - 1] else null
        val effectiveDuration = clampDuration(
            requestedDurationMs = incoming.transitionDurationMs,
            outgoingClip = outgoing,
            incomingClip = incoming,
            transition = incoming.transitionIn
        )
        if (effectiveDuration <= 0L) return null

        val elapsedInClip = (timelinePositionMs - incoming.timelineStartMs).coerceAtLeast(0L)
        if (elapsedInClip >= effectiveDuration) return null

        val rawProgress = (elapsedInClip.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)
        val easedProgress = rawProgress * rawProgress * (3f - 2f * rawProgress)
        return ActiveTransitionState(
            outgoingClip = outgoing,
            incomingClip = incoming,
            transitionType = incoming.transitionIn,
            durationMs = effectiveDuration,
            rawProgress = rawProgress,
            easedProgress = easedProgress
        )
    }

    fun transitionGlyph(transition: TransitionType): String = when (transition) {
        TransitionType.NONE -> "+"
        TransitionType.CROSSFADE -> "⧗"
        TransitionType.DIP_TO_BLACK -> "⬛"
        TransitionType.DIP_TO_WHITE -> "⬜"
        TransitionType.WIPE_LEFT, TransitionType.WIPE_UP -> "⇦"
        TransitionType.WIPE_RIGHT, TransitionType.WIPE_DOWN -> "⇨"
        TransitionType.SLIDE_LEFT, TransitionType.SLIDE_RIGHT,
        TransitionType.SLIDE_UP, TransitionType.SLIDE_DOWN -> "➔"
        TransitionType.ZOOM_IN, TransitionType.ZOOM_OUT -> "⌕"
        TransitionType.IRIS_CIRCLE -> "◉"
        TransitionType.GLITCH_FLASH -> "⚡"
    }
}
