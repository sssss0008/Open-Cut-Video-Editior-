package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.example.data.model.TransitionType

@Composable
fun TransitionsLibraryPanel(
    selectedTransition: TransitionType,
    onSelectTransition: (TransitionType) -> Unit,
    transitionDurationSec: Float,
    onDurationChange: (Float) -> Unit,
    onStartDragTransition: (TransitionType, durationMs: Long) -> Unit,
    onStopDragTransition: () -> Unit,
    onApplyToSelectedClip: () -> Unit,
    onApplyToAllClips: () -> Unit,
    onClose: () -> Unit,
    onDragTransitionMove: (Offset) -> Unit = {},
    onDragTransitionEnd: () -> Unit = onStopDragTransition,
    onOpenFullInspector: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TransitionsLibrary(
        selectedTransition = selectedTransition,
        onSelectTransition = onSelectTransition,
        transitionDurationSec = transitionDurationSec,
        onDurationChange = onDurationChange,
        onDragTransitionStart = { transition, durMs, _ ->
            onStartDragTransition(transition, durMs)
        },
        onDragTransitionMove = onDragTransitionMove,
        onDragTransitionEnd = onDragTransitionEnd,
        onDragTransitionCancel = onStopDragTransition,
        onApplyToSelectedClip = onApplyToSelectedClip,
        onApplyToAllClips = onApplyToAllClips,
        onOpenFullInspector = onOpenFullInspector,
        onClose = onClose,
        modifier = modifier
    )
}
