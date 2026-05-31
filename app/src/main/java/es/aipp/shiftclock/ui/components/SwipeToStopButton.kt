package es.aipp.shiftclock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SwipeToStopButton(
    text: String,
    swipeDirection: String, // "LEFT", "RIGHT", "BOTH"
    onDismiss: () -> Unit
) {
    val width = 250.dp
    val height = 80.dp
    val thumbSize = 80.dp
    val density = LocalDensity.current
    val maxPx = with(density) { (width - thumbSize).toPx() }

    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(height / 2))
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onError
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = when (swipeDirection) {
                "LEFT" -> Alignment.CenterEnd
                "RIGHT" -> Alignment.CenterStart
                else -> Alignment.Center
            }
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .size(thumbSize - 8.dp)
                    .clip(RoundedCornerShape((thumbSize - 8.dp) / 2))
                    .background(MaterialTheme.colorScheme.onError)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val newOffset = offsetX + delta
                            if (swipeDirection == "RIGHT") {
                                offsetX = newOffset.coerceIn(0f, maxPx)
                            } else if (swipeDirection == "LEFT") {
                                offsetX = newOffset.coerceIn(-maxPx, 0f)
                            } else {
                                offsetX = newOffset.coerceIn(-maxPx / 2, maxPx / 2)
                            }
                        },
                        onDragStopped = {
                            val target = if (swipeDirection == "RIGHT") maxPx else if (swipeDirection == "LEFT") -maxPx else maxPx / 2
                            val threshold = if (swipeDirection == "BOTH") maxPx / 3 else maxPx * 0.7f
                            
                            if (Math.abs(offsetX) > threshold) {
                                offsetX = if (offsetX > 0) target else -target
                                onDismiss()
                            } else {
                                offsetX = 0f
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row {
                    if (swipeDirection == "LEFT" || swipeDirection == "BOTH") {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                    if (swipeDirection == "RIGHT" || swipeDirection == "BOTH") {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
