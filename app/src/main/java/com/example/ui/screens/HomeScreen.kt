package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DayPlanEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyTab
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    onNavigateToPlanner: (Int) -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToRevision: () -> Unit
) {
    val dayPlans by viewModel.dayPlans.collectAsState()
    val selectedDayNumber by viewModel.selectedDay.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val testBooks by viewModel.testBooks.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val daysRemaining by viewModel.daysRemaining.collectAsState()
    val overallProgress by viewModel.overallProgressPercent.collectAsState()
    val totalCompletedMins by viewModel.totalCompletedMinutes.collectAsState()

    val currentDayPlan = remember(dayPlans, selectedDayNumber) {
        dayPlans.find { it.dayNumber == selectedDayNumber } ?: dayPlans.firstOrNull() ?: DayPlanEntity(
            dayNumber = selectedDayNumber,
            phase = if (selectedDayNumber <= 10) 1 else if (selectedDayNumber <= 20) 2 else if (selectedDayNumber <= 26) 3 else 4,
            phaseTitle = if (selectedDayNumber <= 10) "PHASE 1: Foundation & Understanding" else "PHASE 2: Practice & Test Books",
            weekNumber = ((selectedDayNumber - 1) / 7 + 1).coerceIn(1, 4),
            subject1Id = "math",
            subject2Id = "english",
            goal = "Master algebra foundation & English grammar",
            plannedMinutes = 120,
            completedMinutes = 0
        )
    }

    val todayTasks = remember(allTasks, selectedDayNumber) {
        allTasks.filter { it.dayNumber == selectedDayNumber }
    }

    val todayCompletedTasksCount = remember(todayTasks) {
        todayTasks.count { it.isCompleted }
    }

    val todayProgressPercent = remember(todayTasks, todayCompletedTasksCount) {
        if (todayTasks.isEmpty()) 0 else ((todayCompletedTasksCount.toFloat() / todayTasks.size) * 100).toInt()
    }

    val remainingBooksCount = remember(testBooks) {
        testBooks.count { it.completedQuestions < it.totalQuestions }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Big Countdown Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("countdown_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(StudyAccentRose)
                                )
                                Text(
                                    text = "$daysRemaining DAYS TO GO",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "30-Day Plan",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "\"Small progress every day becomes a big result.\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Study Target: 60 Hours",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${totalCompletedMins / 60}h ${totalCompletedMins % 60}m completed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedProgressBar(
                            progress = overallProgress / 100f,
                            color = MaterialTheme.colorScheme.primary,
                            height = 8.dp
                        )
                    }
                }
            }
        }

        // 2. Quick 30-Day Selector Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "30-Day Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View Planner >",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToPlanner(selectedDayNumber) }
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(30) { index ->
                        val dayNum = index + 1
                        val isSelected = dayNum == selectedDayNumber
                        val planForDay = dayPlans.find { it.dayNumber == dayNum }
                        val isCompleted = planForDay?.isCompleted == true || (planForDay?.completedMinutes ?: 0) >= 120

                        val containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isCompleted -> StudyAccentEmerald.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val contentColor = when {
                            isSelected -> Color.White
                            isCompleted -> StudyAccentEmerald
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectDay(dayNum) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "DAY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = contentColor.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = String.format("%02d", dayNum),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = contentColor
                                )
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = StudyAccentEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Today's Study Mission Card
        item {
            currentDayPlan?.let { plan ->
                val sub1 = subjects.find { it.id == plan.subject1Id }
                val sub2 = subjects.find { it.id == plan.subject2Id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_mission_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "DAY ${String.format("%02d", plan.dayNumber)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                PhaseBadge(phase = plan.phase)
                            }

                            // Circular Progress for the day
                            CircularProgressRing(
                                progress = todayProgressPercent / 100f,
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 5.dp,
                                progressColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "$todayProgressPercent%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Today's Study Goal
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Today's Study Mission",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = plan.goal,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Two subjects paired for today
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            sub1?.let { s1 ->
                                val color = getSubjectColor(s1.id)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color.copy(alpha = 0.12f))
                                        .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Subject 1 (${s1.category})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = color
                                        )
                                        Text(
                                            text = s1.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            sub2?.let { s2 ->
                                val color = getSubjectColor(s2.id)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color.copy(alpha = 0.12f))
                                        .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Subject 2 (${s2.category})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = color
                                        )
                                        Text(
                                            text = s2.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // 2-Hour Study Structure Action Button
                        Button(
                            onClick = { viewModel.openTimerDialog(plan.subject1Id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_2h_study_session_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start 2-Hour Study Session (Timer)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // 4. Today's Checklist Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Checklist ($todayCompletedTasksCount/${todayTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onNavigateToPlanner(selectedDayNumber) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Edit in Planner",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No tasks set for Day $selectedDayNumber. Tap to add study tasks in Planner.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                val subColor = getSubjectColor(task.subjectId)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_${task.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTask(task) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTask(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("task_checkbox_${task.id}")
                        )

                        CategoryIcon(
                            category = task.category,
                            tint = subColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.isCompleted)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = getSubjectDisplayName(task.subjectId),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = subColor
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${task.plannedMinutes} min",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (task.isWeakTopic) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StudyAccentRose.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Weak Topic",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = StudyAccentRose
                                        )
                                    }
                                }
                            }
                        }

                        // Difficulty Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = task.difficulty.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Stat Row: Test Books Remaining & Streak
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Test Books",
                    value = "${10 - remainingBooksCount}/10",
                    subtitle = "$remainingBooksCount remaining to complete",
                    icon = Icons.Filled.AutoStories,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Daily Streak",
                    value = "${userSettings?.streakCount ?: 0} Days",
                    subtitle = "Tap to advance streak",
                    icon = Icons.Filled.LocalFireDepartment,
                    iconTint = StudyAccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.incrementStreakManual() }
                )
            }
        }

        // 6. 10-Subject Quick Progress Grid / Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10 Subjects Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Explore All >",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToSubjects() }
                )
            }
        }

        items(subjects) { subject ->
            val color = getSubjectColor(subject.id)
            val completedHours = subject.completedMinutes / 60f
            val percent = ((subject.completedMinutes.toDouble() / (subject.targetHours * 60)) * 100).toInt().coerceIn(0, 100)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSubjects() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.1f", completedHours)}/${subject.targetHours}h",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        AnimatedProgressBar(
                            progress = percent / 100f,
                            color = color,
                            height = 6.dp
                        )
                    }

                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
