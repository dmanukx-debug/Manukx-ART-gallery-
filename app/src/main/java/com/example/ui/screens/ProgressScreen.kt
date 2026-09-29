package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PastPaperEntity
import com.example.data.model.TestBookEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: StudyViewModel,
    onEditTestBook: (TestBookEntity) -> Unit,
    onAddPastPaper: () -> Unit,
    onEditPastPaper: (PastPaperEntity) -> Unit
) {
    val overallProgress by viewModel.overallProgressPercent.collectAsState()
    val totalCompletedMinutes by viewModel.totalCompletedMinutes.collectAsState()
    val daysRemaining by viewModel.daysRemaining.collectAsState()
    val testBooks by viewModel.testBooks.collectAsState()
    val pastPapers by viewModel.pastPapers.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val mindMaps by viewModel.mindMaps.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val dayPlans by viewModel.dayPlans.collectAsState()

    var selectedWeeklyTab by remember { mutableIntStateOf(1) } // 1, 2, 3, 4
    var activeProgressSection by remember { mutableStateOf("Overview") } // "Overview", "Test Books", "Past Papers", "Achievements"

    val completedHours = totalCompletedMinutes / 60f
    val remainingHours = (60f - completedHours).coerceAtLeast(0f)
    val completedTestBooksCount = remember(testBooks) {
        testBooks.count { it.completedQuestions >= it.totalQuestions && it.totalQuestions > 0 }
    }
    val completedPastPapersCount = remember(pastPapers) {
        pastPapers.count { it.isCompleted }
    }
    val completedMockTestsCount = remember(allTasks) {
        allTasks.count { it.category == "MOCK_TEST" && it.isCompleted }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("progress_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Selector Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = when (activeProgressSection) {
                    "Overview" -> 0
                    "Weekly" -> 1
                    "Test Books" -> 2
                    "Past Papers" -> 3
                    else -> 4
                },
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeProgressSection == "Overview",
                    onClick = { activeProgressSection = "Overview" },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = activeProgressSection == "Weekly",
                    onClick = { activeProgressSection = "Weekly" },
                    text = { Text("4 Weeks") }
                )
                Tab(
                    selected = activeProgressSection == "Test Books",
                    onClick = { activeProgressSection = "Test Books" },
                    text = { Text("10 Test Books") }
                )
                Tab(
                    selected = activeProgressSection == "Past Papers",
                    onClick = { activeProgressSection = "Past Papers" },
                    text = { Text("Past Papers") }
                )
                Tab(
                    selected = activeProgressSection == "Achievements",
                    onClick = { activeProgressSection = "Achievements" },
                    text = { Text("Badges") }
                )
            }
        }

        when (activeProgressSection) {
            "Overview" -> {
                // 1. Big Circular Progress Hero
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "30-Day Master Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            CircularProgressRing(
                                progress = overallProgress / 100f,
                                modifier = Modifier.size(130.dp),
                                strokeWidth = 12.dp,
                                progressColor = MaterialTheme.colorScheme.primary
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$overallProgress%",
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Completed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Total Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "60 Hours", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${String.format("%.1f", completedHours)}h", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StudyAccentEmerald)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${String.format("%.1f", remainingHours)}h", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StudyAccentAmber)
                                }
                            }
                        }
                    }
                }

                // 2. Comprehensive Metrics Grid
                item {
                    Text(
                        text = "Key Study Deliverables",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Test Books",
                            value = "$completedTestBooksCount/10",
                            subtitle = "Full-test books done",
                            icon = Icons.Filled.AutoStories,
                            iconTint = StudyPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Past Papers",
                            value = "$completedPastPapersCount Papers",
                            subtitle = "Completed & corrected",
                            icon = Icons.Filled.Description,
                            iconTint = StudySecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Short Notes",
                            value = "${notes.size} Notes",
                            subtitle = "Subject summaries",
                            icon = Icons.Filled.EditNote,
                            iconTint = StudyAccentAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Mind Maps",
                            value = "${mindMaps.size} Maps",
                            subtitle = "Visual diagram trees",
                            icon = Icons.Filled.AccountTree,
                            iconTint = StudyAccentPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Mock Tests",
                            value = "$completedMockTestsCount Mock",
                            subtitle = "Exam timed simulations",
                            icon = Icons.Filled.EmojiEvents,
                            iconTint = StudyAccentEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Countdown",
                            value = "$daysRemaining Days",
                            subtitle = "Until Final Exam",
                            icon = Icons.Filled.HourglassBottom,
                            iconTint = StudyAccentRose,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            "Weekly" -> {
                // 4 Weekly Dashboards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1 to "Week 1\nFoundation",
                            2 to "Week 2\nPractice",
                            3 to "Week 3\nPast Papers",
                            4 to "Week 4\nFinal Mock"
                        ).forEach { (wNum, label) ->
                            val isSelected = selectedWeeklyTab == wNum
                            Button(
                                onClick = { selectedWeeklyTab = wNum },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item {
                    val daysInWeek = when (selectedWeeklyTab) {
                        1 -> 1..7
                        2 -> 8..15
                        3 -> 16..22
                        else -> 23..30
                    }
                    val weekTitle = when (selectedWeeklyTab) {
                        1 -> "Week 1 – Build the Foundation (Days 1–7)"
                        2 -> "Week 2 – Complete & Practice (Days 8–15)"
                        3 -> "Week 3 – Past Papers & Weak Areas (Days 16–22)"
                        else -> "Week 4 – Final Revision & Mock Tests (Days 23–30)"
                    }

                    val weekPlans = dayPlans.filter { it.dayNumber in daysInWeek }
                    val weekTasks = allTasks.filter { it.dayNumber in daysInWeek }
                    val weekCompletedMinutes = weekPlans.sumOf { it.completedMinutes }
                    val weekPlannedMinutes = weekPlans.sumOf { it.plannedMinutes }
                    val weekTasksDone = weekTasks.count { it.isCompleted }
                    val weekTasksRemaining = weekTasks.size - weekTasksDone
                    val subjectsStudied = weekPlans.flatMap { listOf(it.subject1Id, it.subject2Id) }.distinct()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = weekTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Week Progress Bar
                            val weekProgress = if (weekPlannedMinutes > 0) weekCompletedMinutes.toFloat() / weekPlannedMinutes else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Hours Completed", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${weekCompletedMinutes / 60}h ${weekCompletedMinutes % 60}m / ${weekPlannedMinutes / 60}h",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AnimatedProgressBar(progress = weekProgress, color = MaterialTheme.colorScheme.primary, height = 8.dp)

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Subjects Studied in Week
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "Subjects Scheduled (${subjectsStudied.size}):", style = MaterialTheme.typography.labelSmall)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(subjectsStudied) { subId ->
                                        SubjectBadge(subjectName = getSubjectDisplayName(subId), color = getSubjectColor(subId))
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            // Tasks breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Tasks Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "$weekTasksDone tasks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StudyAccentEmerald)
                                }
                                Column {
                                    Text(text = "Remaining Tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "$weekTasksRemaining tasks", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StudyAccentAmber)
                                }
                                Column {
                                    Text(text = "Week Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${daysInWeek.count()} Days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            "Test Books" -> {
                // 10 Full-Test Books Section
                item {
                    Text(
                        text = "10 Full-Test Books Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(testBooks, key = { it.id }) { book ->
                    val color = getSubjectColor(book.subjectId)
                    val bookProgress = if (book.totalQuestions > 0) book.completedQuestions.toFloat() / book.totalQuestions else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditTestBook(book) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            .background(color)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Book ${String.format("%02d", book.bookNumber)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { onEditTestBook(book) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subject: ${getSubjectDisplayName(book.subjectId)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                                Text(
                                    text = "${book.completedQuestions}/${book.totalQuestions} Questions",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AnimatedProgressBar(progress = bookProgress, color = color, height = 6.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Score: ${book.scorePercentage}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (book.scorePercentage >= 75) StudyAccentEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Mistakes: ${book.mistakesCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (book.mistakesCount > 0) StudyAccentRose else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Status: ${book.revisionStatus.replace('_', ' ')}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            "Past Papers" -> {
                // Past Papers Section with Score Trend Chart
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Past Paper Tracker & Score Trends",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = onAddPastPaper,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Paper", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Score Trends Chart Card
                item {
                    val completedPapers = pastPapers.filter { it.isCompleted }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Score Trajectory (%)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (completedPapers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No completed past papers yet. Log your first test score to see trends!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(vertical = 8.dp)
                                ) {
                                    val scores = completedPapers.map { it.score.toFloat() }
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val width = size.width
                                        val height = size.height
                                        val maxScoreVal = 100f
                                        val stepX = if (scores.size > 1) width / (scores.size - 1) else width / 2

                                        val path = Path()
                                        scores.forEachIndexed { i, s ->
                                            val x = if (scores.size > 1) i * stepX else width / 2
                                            val y = height - (s / maxScoreVal * height)
                                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                            drawCircle(color = Color(0xFF6366F1), radius = 4.dp.toPx(), center = Offset(x, y))
                                        }
                                        drawPath(path = path, color = Color(0xFF6366F1), style = Stroke(width = 2.5.dp.toPx()))
                                    }
                                }
                            }
                        }
                    }
                }

                if (pastPapers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No past papers added. Tap '+ Log Paper' to track your test papers.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(pastPapers, key = { it.id }) { paper ->
                        val subColor = getSubjectColor(paper.subjectId)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditPastPaper(paper) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${getSubjectDisplayName(paper.subjectId)} - ${paper.year}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${paper.term} • Time: ${paper.timeTakenMinutes} mins",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (paper.mistakes.isNotBlank()) {
                                        Text(
                                            text = "Mistakes note: ${paper.mistakes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StudyAccentRose,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${paper.score}/${paper.maxScore}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (paper.score >= 75) StudyAccentEmerald else MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (paper.isCompleted) StudyAccentEmerald.copy(alpha = 0.15f) else StudyAccentAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (paper.isCompleted) "Completed" else "Pending",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (paper.isCompleted) StudyAccentEmerald else StudyAccentAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Achievements" -> {
                // Gamification Achievements
                item {
                    Text(
                        text = "Study Consistency & Milestone Badges",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(achievements, key = { it.id }) { ach ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (ach.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (ach.isUnlocked) StudyAccentAmber.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = ach.iconEmoji, style = MaterialTheme.typography.titleMedium)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = ach.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (ach.isUnlocked) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Unlocked",
                                    tint = StudyAccentEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
