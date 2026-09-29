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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MindMapEntity
import com.example.data.model.StudyNoteEntity
import com.example.data.model.WeakAreaEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(
    viewModel: StudyViewModel,
    onAddWeakArea: () -> Unit,
    onAddNote: () -> Unit,
    onEditNote: (StudyNoteEntity) -> Unit,
    onAddMindMap: () -> Unit,
    onEditMindMap: (MindMapEntity) -> Unit
) {
    val weakAreas by viewModel.weakAreas.collectAsState()
    val allTasks by viewModel.tasks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val mindMaps by viewModel.mindMaps.collectAsState()

    var activeTab by remember { mutableStateOf("Weak Areas") } // "Weak Areas", "Short Notes", "Mind Maps"
    var weakFilter by remember { mutableStateOf("All") } // "All", "Weak Topics", "To Revise", "Mistakes to Fix", "High Priority"

    val highPriorityTasks = remember(allTasks) {
        allTasks.filter { it.priority == "HIGH" && !it.isCompleted }
    }

    val filteredWeakAreas = remember(weakAreas, weakFilter) {
        when (weakFilter) {
            "Weak Topics" -> weakAreas.filter { it.type == "WEAK_TOPIC" }
            "To Revise" -> weakAreas.filter { it.type == "TO_REVISE" }
            "Mistakes to Fix" -> weakAreas.filter { it.type == "MISTAKE_TO_FIX" }
            "High Priority" -> weakAreas.filter { it.type == "HIGH_PRIORITY" || it.severity == "HIGH" }
            else -> weakAreas
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeTab) {
                        "Weak Areas" -> onAddWeakArea()
                        "Short Notes" -> onAddNote()
                        "Mind Maps" -> onAddMindMap()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_revision_add")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Navigation Tabs (Weak Areas, Short Notes, Mind Maps)
            PrimaryTabRow(
                selectedTabIndex = when (activeTab) {
                    "Weak Areas" -> 0
                    "Short Notes" -> 1
                    else -> 2
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == "Weak Areas",
                    onClick = { activeTab = "Weak Areas" },
                    text = { Text("🚨 Weak Areas") }
                )
                Tab(
                    selected = activeTab == "Short Notes",
                    onClick = { activeTab = "Short Notes" },
                    text = { Text("📝 Short Notes (${notes.size})") }
                )
                Tab(
                    selected = activeTab == "Mind Maps",
                    onClick = { activeTab = "Mind Maps" },
                    text = { Text("🧠 Mind Maps (${mindMaps.size})") }
                )
            }

            when (activeTab) {
                "Weak Areas" -> {
                    // Filter row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "Weak Topics", "To Revise", "Mistakes to Fix", "High Priority")
                        items(filters) { f ->
                            FilterChip(
                                selected = weakFilter == f,
                                onClick = { weakFilter = f },
                                label = { Text(f, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("weak_areas_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // High Priority Tasks Banner
                        if (highPriorityTasks.isNotEmpty() && (weakFilter == "All" || weakFilter == "High Priority")) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = StudyAccentRose.copy(alpha = 0.12f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PriorityHigh,
                                                contentDescription = null,
                                                tint = StudyAccentRose,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "High Priority Study Tasks (${highPriorityTasks.size})",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = StudyAccentRose
                                            )
                                        }
                                        Text(
                                            text = "Give extra revision time to these key concepts to maximize test scores.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredWeakAreas.isEmpty() && highPriorityTasks.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "No weak topics flagged yet. Tap '+' to record tricky questions or formulas that need revision.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredWeakAreas, key = { it.id }) { area ->
                                val subColor = getSubjectColor(area.subjectId)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (area.isResolved)
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                                Checkbox(
                                                    checked = area.isResolved,
                                                    onCheckedChange = { viewModel.resolveWeakArea(area) }
                                                )
                                                Column {
                                                    Text(
                                                        text = area.topic,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            textDecoration = if (area.isResolved) TextDecoration.LineThrough else TextDecoration.None
                                                        ),
                                                        color = if (area.isResolved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = getSubjectDisplayName(area.subjectId),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = subColor
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteWeakArea(area) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        if (area.notes.isNotBlank()) {
                                            Text(
                                                text = area.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (area.severity) {
                                                            "HIGH" -> StudyAccentRose.copy(alpha = 0.15f)
                                                            "MEDIUM" -> StudyAccentAmber.copy(alpha = 0.15f)
                                                            else -> StudyAccentEmerald.copy(alpha = 0.15f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Severity: ${area.severity}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = when (area.severity) {
                                                        "HIGH" -> StudyAccentRose
                                                        "MEDIUM" -> StudyAccentAmber
                                                        else -> StudyAccentEmerald
                                                    }
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.openTimerDialog(area.subjectId) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Revise Now", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Short Notes" -> {
                    // Short Notes Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("short_notes_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (notes.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "No notes recorded yet. Tap '+' to create concise revision notes with formulas, summaries, and key points.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(notes, key = { it.id }) { note ->
                                val subColor = getSubjectColor(note.subjectId)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditNote(note) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SubjectBadge(subjectName = getSubjectDisplayName(note.subjectId), color = subColor)
                                            Row {
                                                IconButton(onClick = { onEditNote(note) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(onClick = { viewModel.deleteNote(note) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Text(
                                            text = note.topic,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = note.summary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (note.formulas.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Formula: ${note.formulas}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Mind Maps" -> {
                    // Mind Maps Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("mind_maps_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (mindMaps.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "No mind maps created yet. Tap '+' to structure high-yield concepts into visual branches.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(mindMaps, key = { it.id }) { map ->
                                val subColor = getSubjectColor(map.subjectId)
                                val branchList = remember(map.branchesText) {
                                    map.branchesText.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditMindMap(map) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SubjectBadge(subjectName = getSubjectDisplayName(map.subjectId), color = subColor)
                                            IconButton(onClick = { viewModel.deleteMindMap(map) }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Text(
                                            text = map.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        // Central Idea Hub
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(subColor.copy(alpha = 0.15f))
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Central Idea: ${map.mainIdea}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = subColor
                                            )
                                        }

                                        // Branch Nodes Preview
                                        Text(text = "Key Branches & Nodes:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            branchList.take(3).forEach { branch ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .border(1.dp, subColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = branch,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                            if (branchList.size > 3) {
                                                Text(
                                                    text = "+${branchList.size - 3} more",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.align(Alignment.CenterVertically)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
