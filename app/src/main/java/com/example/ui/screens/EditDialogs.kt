package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.getSubjectDisplayName
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    dayNumber: Int,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: String, title: String, category: String, minutes: Int, priority: String, difficulty: String, isWeak: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "math") }
    var selectedCategory by remember { mutableStateOf("STUDY") }
    var plannedMinutesText by remember { mutableStateOf("25") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var difficulty by remember { mutableStateOf("MEDIUM") }
    var isWeakTopic by remember { mutableStateOf(false) }

    val categories = listOf("STUDY", "SHORT_NOTE", "MIND_MAP", "TEST_BOOK", "PAST_PAPER", "REVISION", "MISTAKE_CORRECTION", "MOCK_TEST")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")
    val difficulties = listOf("EASY", "MEDIUM", "HARD")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Task for Day $dayNumber",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title / Topic") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject Selector
                Text("Subject:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(
                        text = getSubjectDisplayName(selectedSubjectId),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category Chips
                Text("Task Category:", style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.replace('_', ' '), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.drop(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.replace('_', ' '), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Minutes
                OutlinedTextField(
                    value = plannedMinutesText,
                    onValueChange = { plannedMinutesText = it },
                    label = { Text("Planned Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority & Difficulty
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Priority:", style = MaterialTheme.typography.labelSmall)
                        priorities.forEach { p ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = priority == p, onClick = { priority = p })
                                Text(p, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Difficulty:", style = MaterialTheme.typography.labelSmall)
                        difficulties.forEach { d ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = difficulty == d, onClick = { difficulty = d })
                                Text(d, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isWeakTopic, onCheckedChange = { isWeakTopic = it })
                    Text("Flag as Difficult / Weak Topic", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    selectedSubjectId,
                                    title.trim(),
                                    selectedCategory,
                                    plannedMinutesText.toIntOrNull() ?: 25,
                                    priority,
                                    difficulty,
                                    isWeakTopic
                                )
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}

@Composable
fun EditGoalDialog(
    dayPlan: DayPlanEntity,
    onDismiss: () -> Unit,
    onSave: (DayPlanEntity) -> Unit
) {
    var goalText by remember { mutableStateOf(dayPlan.goal) }
    var notesText by remember { mutableStateOf(dayPlan.notes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Edit Goal for Day ${dayPlan.dayNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Study Mission / Goal") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Day Reflection / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        onSave(dayPlan.copy(goal = goalText.trim(), notes = notesText.trim()))
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun EditTestBookDialog(
    testBook: TestBookEntity,
    onDismiss: () -> Unit,
    onSave: (TestBookEntity) -> Unit
) {
    var title by remember { mutableStateOf(testBook.title) }
    var totalQuestionsText by remember { mutableStateOf(testBook.totalQuestions.toString()) }
    var completedQuestionsText by remember { mutableStateOf(testBook.completedQuestions.toString()) }
    var scoreText by remember { mutableStateOf(testBook.scorePercentage.toString()) }
    var mistakesText by remember { mutableStateOf(testBook.mistakesCount.toString()) }
    var mistakesNotes by remember { mutableStateOf(testBook.mistakesNotes) }
    var revisionStatus by remember { mutableStateOf(testBook.revisionStatus) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Edit Test Book ${testBook.bookNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Test Book Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = completedQuestionsText,
                        onValueChange = { completedQuestionsText = it },
                        label = { Text("Completed Qs") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalQuestionsText,
                        onValueChange = { totalQuestionsText = it },
                        label = { Text("Total Qs") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        label = { Text("Score %") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mistakesText,
                        onValueChange = { mistakesText = it },
                        label = { Text("Mistakes Count") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = mistakesNotes,
                    onValueChange = { mistakesNotes = it },
                    label = { Text("Mistake Correction Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val comp = completedQuestionsText.toIntOrNull() ?: 0
                            val tot = (totalQuestionsText.toIntOrNull() ?: 100).coerceAtLeast(1)
                            val status = if (comp >= tot) "COMPLETED" else if (comp > 0) "IN_PROGRESS" else "NOT_STARTED"
                            onSave(
                                testBook.copy(
                                    title = title.trim(),
                                    completedQuestions = comp,
                                    totalQuestions = tot,
                                    scorePercentage = scoreText.toIntOrNull() ?: 0,
                                    mistakesCount = mistakesText.toIntOrNull() ?: 0,
                                    mistakesNotes = mistakesNotes.trim(),
                                    revisionStatus = status,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPastPaperDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: String, year: String, term: String, score: Int, timeTaken: Int, mistakes: String, isCompleted: Boolean) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "math") }
    var year by remember { mutableStateOf("2024") }
    var term by remember { mutableStateOf("3rd Term / Final") }
    var scoreText by remember { mutableStateOf("0") }
    var timeTakenText by remember { mutableStateOf("60") }
    var mistakes by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Log Past Paper",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text("Subject: ${getSubjectDisplayName(selectedSubjectId)}", style = MaterialTheme.typography.labelSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = term,
                        onValueChange = { term = it },
                        label = { Text("Term / Type") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        label = { Text("Score / 100") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeTakenText,
                        onValueChange = { timeTakenText = it },
                        label = { Text("Time (Minutes)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = mistakes,
                    onValueChange = { mistakes = it },
                    label = { Text("Mistakes / Weak Areas in Paper") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCompleted, onCheckedChange = { isCompleted = it })
                    Text("Completed & Graded", style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            onSave(
                                selectedSubjectId,
                                year.trim(),
                                term.trim(),
                                scoreText.toIntOrNull() ?: 0,
                                timeTakenText.toIntOrNull() ?: 60,
                                mistakes.trim(),
                                isCompleted
                            )
                        }
                    ) {
                        Text("Save Record")
                    }
                }
            }
        }
    }
}

@Composable
fun AddStudyNoteDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: String, topic: String, summary: String, formulas: String, facts: String, keywords: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "math") }
    var topic by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var formulas by remember { mutableStateOf("") }
    var facts by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "New Short Revision Note",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = formulas,
                    onValueChange = { formulas = it },
                    label = { Text("Formulas / Rules") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = facts,
                    onValueChange = { facts = it },
                    label = { Text("Important Facts") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Keywords") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (topic.isNotBlank()) {
                                onSave(selectedSubjectId, topic.trim(), summary.trim(), formulas.trim(), facts.trim(), keywords.trim())
                            }
                        },
                        enabled = topic.isNotBlank()
                    ) {
                        Text("Save Note")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMindMapDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: String, title: String, mainIdea: String, branches: String, keywords: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "math") }
    var title by remember { mutableStateOf("") }
    var mainIdea by remember { mutableStateOf("") }
    var branches by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "New Visual Mind Map",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mind Map Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mainIdea,
                    onValueChange = { mainIdea = it },
                    label = { Text("Central Idea / Concept") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = branches,
                    onValueChange = { branches = it },
                    label = { Text("Branches (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("Keywords") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(selectedSubjectId, title.trim(), mainIdea.trim(), branches.trim(), keywords.trim())
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Create Map")
                    }
                }
            }
        }
    }
}

@Composable
fun AddWeakAreaDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: String, topic: String, type: String, severity: String, notes: String) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "math") }
    var topic by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("WEAK_TOPIC") }
    var selectedSeverity by remember { mutableStateOf("HIGH") }
    var notes by remember { mutableStateOf("") }

    val types = listOf("WEAK_TOPIC", "TO_REVISE", "MISTAKE_TO_FIX", "HIGH_PRIORITY")
    val severities = listOf("HIGH", "MEDIUM", "LOW")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Weak Area / Difficult Topic",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Tricky Topic / Mistake Concept") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Revision Category:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.replace('_', ' '), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Severity:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    severities.forEach { s ->
                        FilterChip(
                            selected = selectedSeverity == s,
                            onClick = { selectedSeverity = s },
                            label = { Text(s, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("What made it difficult?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (topic.isNotBlank()) {
                                onSave(selectedSubjectId, topic.trim(), selectedType, selectedSeverity, notes.trim())
                            }
                        },
                        enabled = topic.isNotBlank()
                    ) {
                        Text("Add to Revision")
                    }
                }
            }
        }
    }
}

@Composable
fun EditStudyBlocksDialog(
    studyBlocks: List<StudyBlockEntity>,
    onDismiss: () -> Unit,
    onSaveBlock: (StudyBlockEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Customize 2-Hour Study Blocks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Adjust the 25m/5m Pomodoro cycles to match your energy level.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                studyBlocks.forEach { block ->
                    var durationText by remember(block.durationMinutes) { mutableStateOf(block.durationMinutes.toString()) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = block.title,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = {
                                durationText = it
                                val d = it.toIntOrNull()
                                if (d != null && d > 0) {
                                    onSaveBlock(block.copy(durationMinutes = d))
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text("Done") }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    userSettings: UserSettingsEntity?,
    onDismiss: () -> Unit,
    onUpdateDailyGoal: (Int) -> Unit
) {
    var goalText by remember { mutableStateOf((userSettings?.dailyGoalMinutes ?: 120).toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Study Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    label = { Text("Daily Study Target (Minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Total 30-Day Available Hours: 60 Hours (~2h/day). Balanced intelligently across 10 subjects.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    Button(onClick = {
                        val g = goalText.toIntOrNull() ?: 120
                        onUpdateDailyGoal(g)
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
