package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    paperColor: Color = Color(0xFFF3ECE2), // Warm cream toned paper
    onExportBitmapDataUrl: (String) -> Unit
) {
    var paths by remember { mutableStateOf(listOf<DrawingPath>()) }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    var currentColor by remember { mutableStateOf(Color(0xFF1E1E24)) } // Charcoal black
    var currentStrokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }

    val palette = listOf(
        Color(0xFF1E1E24), // Charcoal
        Color(0xFF4A3728), // Raw Umber
        Color(0xFFD97736), // Terracotta
        Color(0xFF1E3A8A), // Indigo
        Color(0xFF991B1B), // Carmine Red
        Color(0xFF065F46), // Viridian Green
        Color(0xFFF9FAFB)  // White Chalk
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                palette.forEach { color ->
                    val isSelected = !isEraser && currentColor == color
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .pointerInput(color) {
                                isEraser = false
                                currentColor = color
                            }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { isEraser = !isEraser },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Brush/Eraser",
                        tint = if (isEraser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            paths = paths.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    enabled = paths.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        paths = emptyList()
                        currentPoints = emptyList()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Stroke Width Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEraser) "Eraser Size" else "Pencil Weight",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = currentStrokeWidth,
                onValueChange = { currentStrokeWidth = it },
                valueRange = 2f..28f,
                modifier = Modifier.weight(1f)
            )
        }

        // Canvas Drawing Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(paperColor)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isEraser, currentColor, currentStrokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    paths = paths + DrawingPath(
                                        points = currentPoints,
                                        color = if (isEraser) paperColor else currentColor,
                                        strokeWidth = currentStrokeWidth,
                                        isEraser = isEraser
                                    )
                                    currentPoints = emptyList()
                                }
                            },
                            onDragCancel = {
                                currentPoints = emptyList()
                            }
                        )
                    }
            ) {
                // Draw completed paths
                paths.forEach { path ->
                    if (path.points.size > 1) {
                        for (i in 0 until path.points.size - 1) {
                            drawLine(
                                color = path.color,
                                start = path.points[i],
                                end = path.points[i + 1],
                                strokeWidth = path.strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    } else if (path.points.isNotEmpty()) {
                        drawCircle(
                            color = path.color,
                            radius = path.strokeWidth / 2,
                            center = path.points[0]
                        )
                    }
                }

                // Draw current in-progress path
                if (currentPoints.size > 1) {
                    val activeColor = if (isEraser) paperColor else currentColor
                    for (i in 0 until currentPoints.size - 1) {
                        drawLine(
                            color = activeColor,
                            start = currentPoints[i],
                            end = currentPoints[i + 1],
                            strokeWidth = currentStrokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            if (paths.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = "Draw your art piece here with touch or stylus...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Generate bitmap and Base64 string
                val width = 600
                val height = 450
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bitmap)
                canvas.drawColor(paperColor.toArgb())

                val paint = AndroidPaint().apply {
                    isAntiAlias = true
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                }

                paths.forEach { path ->
                    paint.color = path.color.toArgb()
                    paint.strokeWidth = path.strokeWidth * (width / 360f)
                    if (path.points.size > 1) {
                        for (i in 0 until path.points.size - 1) {
                            val p1 = path.points[i]
                            val p2 = path.points[i + 1]
                            val sx = p1.x * (width / 360f)
                            val sy = p1.y * (height / 280f)
                            val ex = p2.x * (width / 360f)
                            val ey = p2.y * (height / 280f)
                            canvas.drawLine(sx, sy, ex, ey, paint)
                        }
                    }
                }

                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                onExportBitmapDataUrl("data:image/png;base64,$base64")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Attach This Drawing to Post", style = MaterialTheme.typography.labelLarge)
        }
    }
}
