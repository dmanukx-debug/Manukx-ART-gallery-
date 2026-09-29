package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.getSubjectColor
import com.example.ui.components.getSubjectDisplayName
import com.example.ui.theme.StudyAccentAmber
import com.example.ui.theme.StudyAccentEmerald
import com.example.ui.theme.StudyPrimary
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun StudyTimerDialog(
    viewModel: StudyViewModel,
    onDismiss: () -> Unit
) {
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val blockIndex by viewModel.currentBlockIndex.collectAsState()
    val blocks by viewModel.studyBlocks.collectAsState()
    val timerSubjectId by viewModel.timerSubjectId.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val currentBlock = remember(blocks, blockIndex) {
        if (blocks.indices.contains(blockIndex)) blocks[blockIndex] else null
    }

    val totalDurationSeconds = remember(currentBlock) {
        (currentBlock?.durationMinutes ?: 25) * 60
    }

    val progress = remember(remainingSeconds, totalDurationSeconds) {
        if (totalDurationSeconds > 0) (totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds else 0f
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val subjectColor = getSubjectColor(timerSubjectId)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("study_timer_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "2-Hour Study Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Block ${blockIndex + 1} of ${blocks.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Subject Selector Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Studying Subject:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { s ->
                            val isSelected = s.id == timerSubjectId
                            val sCol = getSubjectColor(s.id)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) sCol else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.setTimerSubject(s.id) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = s.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Current Block Title
                currentBlock?.let { b ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (b.isBreak) StudyAccentAmber.copy(alpha = 0.15f)
                                else subjectColor.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = b.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (b.isBreak) StudyAccentAmber else subjectColor
                        )
                    }
                }

                // Big Animated Progress Ring with Timer Text
                CircularProgressRing(
                    progress = progress,
                    modifier = Modifier.size(170.dp),
                    strokeWidth = 12.dp,
                    progressColor = if (currentBlock?.isBreak == true) StudyAccentAmber else subjectColor
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isRunning) "Focusing..." else "Paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRunning) StudyAccentEmerald else StudyAccentAmber
                        )
                    }
                }

                // Controls: Reset, Play/Pause, Skip Block
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.resetTimerCurrentBlock() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Block")
                    }

                    FilledIconButton(
                        onClick = { viewModel.toggleTimer() },
                        modifier = Modifier.size(60.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (currentBlock?.isBreak == true) StudyAccentAmber else subjectColor
                        )
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.advanceTimerBlock() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Skip Block")
                    }
                }

                // Finish & Log Session Button
                Button(
                    onClick = {
                        val duration = currentBlock?.durationMinutes ?: 25
                        viewModel.finishSessionAndLog(duration)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log This Study Block (${currentBlock?.durationMinutes ?: 25}m)", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
