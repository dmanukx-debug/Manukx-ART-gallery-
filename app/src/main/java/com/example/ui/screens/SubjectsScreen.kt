package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    viewModel: StudyViewModel,
    onOpenSubjectDetail: (SubjectEntity) -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val testBooks by viewModel.testBooks.collectAsState()
    val pastPapers by viewModel.pastPapers.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val mindMaps by viewModel.mindMaps.collectAsState()
    val weakAreas by viewModel.weakAreas.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Heavy", "Language", "Theory", "Creative")

    val filteredSubjects = remember(subjects, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") subjects
        else subjects.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Subject Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("subjects_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "10 Grade 8 Subjects Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(filteredSubjects, key = { it.id }) { subject ->
                    val color = getSubjectColor(subject.id)
                    val completedHours = subject.completedMinutes / 60f
                    val progressPercent = ((subject.completedMinutes.toDouble() / (subject.targetHours * 60)) * 100).toInt().coerceIn(0, 100)

                    val relatedTestBook = testBooks.find { it.subjectId == subject.id }
                    val relatedPapers = pastPapers.filter { it.subjectId == subject.id }
                    val relatedNotes = notes.filter { it.subjectId == subject.id }
                    val relatedMaps = mindMaps.filter { it.subjectId == subject.id }
                    val relatedWeak = weakAreas.filter { it.subjectId == subject.id && !it.isResolved }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_card_${subject.id}")
                            .clickable { onOpenSubjectDetail(subject) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header with Subject Name & Ring
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Column {
                                        Text(
                                            text = subject.name.uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${subject.category} Subject • Priority: ${subject.priority}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                CircularProgressRing(
                                    progress = progressPercent / 100f,
                                    modifier = Modifier.size(46.dp),
                                    strokeWidth = 5.dp,
                                    progressColor = color
                                ) {
                                    Text(
                                        text = "$progressPercent%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Progress Bar
                            AnimatedProgressBar(
                                progress = progressPercent / 100f,
                                color = color,
                                height = 7.dp
                            )

                            // 4 Core Metrics Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBox(
                                    label = "Study Hours",
                                    value = "${String.format("%.1f", completedHours)}/${subject.targetHours}h",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Test Book",
                                    value = if (relatedTestBook != null) "${relatedTestBook.scorePercentage}%" else "0%",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Past Papers",
                                    value = "${relatedPapers.count { it.isCompleted }}/${relatedPapers.size.coerceAtLeast(1)}",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Notes / Maps",
                                    value = "${relatedNotes.size} / ${relatedMaps.size}",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Footer: Quick Start Timer & Weak Indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (relatedWeak.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = StudyAccentRose,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${relatedWeak.size} weak area${if (relatedWeak.size > 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = StudyAccentRose
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "On track with syllabus",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.openTimerDialog(subject.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Study Now", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
