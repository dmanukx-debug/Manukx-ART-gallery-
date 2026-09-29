package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.ui.components.StudyBottomNav
import com.example.ui.components.StudyTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.StudyPlannerTheme
import com.example.ui.viewmodel.StudyTab
import com.example.ui.viewmodel.StudyViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StudyViewModel = viewModel()
            val userSettings by viewModel.userSettings.collectAsState()
            val isDarkTheme = userSettings?.isDarkTheme ?: true
            val currentTab by viewModel.currentTab.collectAsState()
            val weakAreas by viewModel.weakAreas.collectAsState()
            val unresolvedWeakCount = remember(weakAreas) { weakAreas.count { !it.isResolved } }
            val showTimerDialog by viewModel.showTimerDialog.collectAsState()
            val subjects by viewModel.subjects.collectAsState()
            val studyBlocks by viewModel.studyBlocks.collectAsState()

            // Dialog States
            var addTaskDayNumber by remember { mutableStateOf<Int?>(null) }
            var editGoalDayPlan by remember { mutableStateOf<DayPlanEntity?>(null) }
            var editTestBook by remember { mutableStateOf<TestBookEntity?>(null) }
            var showAddPastPaper by remember { mutableStateOf(false) }
            var editPastPaper by remember { mutableStateOf<PastPaperEntity?>(null) }
            var showAddNote by remember { mutableStateOf(false) }
            var editNote by remember { mutableStateOf<StudyNoteEntity?>(null) }
            var showAddMindMap by remember { mutableStateOf(false) }
            var editMindMap by remember { mutableStateOf<MindMapEntity?>(null) }
            var showAddWeakArea by remember { mutableStateOf(false) }
            var showEditStudyBlocks by remember { mutableStateOf(false) }
            var showSettingsDialog by remember { mutableStateOf(false) }
            var inspectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.toastMessage.collectLatest { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }

            StudyPlannerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            StudyTopAppBar(
                                streakCount = userSettings?.streakCount ?: 0,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkTheme() },
                                onOpenTimer = { viewModel.openTimerDialog() },
                                onOpenSettings = { showSettingsDialog = true },
                                onStreakClick = { viewModel.incrementStreakManual() }
                            )
                        },
                        bottomBar = {
                            StudyBottomNav(
                                currentTab = currentTab,
                                weakTopicsCount = unresolvedWeakCount,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "ScreenTransition"
                            ) { tab ->
                                when (tab) {
                                    StudyTab.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToPlanner = { day ->
                                            viewModel.selectDay(day)
                                            viewModel.selectTab(StudyTab.PLANNER)
                                        },
                                        onNavigateToSubjects = { viewModel.selectTab(StudyTab.SUBJECTS) },
                                        onNavigateToRevision = { viewModel.selectTab(StudyTab.REVISION) }
                                    )
                                    StudyTab.PLANNER -> PlannerScreen(
                                        viewModel = viewModel,
                                        onOpenAddTask = { day -> addTaskDayNumber = day },
                                        onOpenEditGoal = { plan -> editGoalDayPlan = plan },
                                        onOpenEditBlocks = { showEditStudyBlocks = true }
                                    )
                                    StudyTab.SUBJECTS -> SubjectsScreen(
                                        viewModel = viewModel,
                                        onOpenSubjectDetail = { sub -> inspectedSubject = sub }
                                    )
                                    StudyTab.PROGRESS -> ProgressScreen(
                                        viewModel = viewModel,
                                        onEditTestBook = { book -> editTestBook = book },
                                        onAddPastPaper = { showAddPastPaper = true },
                                        onEditPastPaper = { paper -> editPastPaper = paper }
                                    )
                                    StudyTab.REVISION -> RevisionScreen(
                                        viewModel = viewModel,
                                        onAddWeakArea = { showAddWeakArea = true },
                                        onAddNote = { showAddNote = true },
                                        onEditNote = { n -> editNote = n },
                                        onAddMindMap = { showAddMindMap = true },
                                        onEditMindMap = { m -> editMindMap = m }
                                    )
                                }
                            }
                        }
                    }

                    // Dialogs
                    if (showTimerDialog) {
                        StudyTimerDialog(
                            viewModel = viewModel,
                            onDismiss = { viewModel.closeTimerDialog() }
                        )
                    }

                    addTaskDayNumber?.let { day ->
                        AddTaskDialog(
                            dayNumber = day,
                            subjects = subjects,
                            onDismiss = { addTaskDayNumber = null },
                            onSave = { subId, title, cat, mins, prio, diff, isWeak ->
                                viewModel.addTask(day, subId, title, cat, mins, prio, diff, isWeak)
                                addTaskDayNumber = null
                            }
                        )
                    }

                    editGoalDayPlan?.let { plan ->
                        EditGoalDialog(
                            dayPlan = plan,
                            onDismiss = { editGoalDayPlan = null },
                            onSave = { updated ->
                                viewModel.updateDayPlan(updated)
                                editGoalDayPlan = null
                            }
                        )
                    }

                    editTestBook?.let { book ->
                        EditTestBookDialog(
                            testBook = book,
                            onDismiss = { editTestBook = null },
                            onSave = { updated ->
                                viewModel.updateTestBook(updated)
                                editTestBook = null
                            }
                        )
                    }

                    if (showAddPastPaper) {
                        AddPastPaperDialog(
                            subjects = subjects,
                            onDismiss = { showAddPastPaper = false },
                            onSave = { subId, year, term, score, time, mistakes, comp ->
                                viewModel.addPastPaper(subId, year, term, score, 100, time, mistakes, comp)
                                showAddPastPaper = false
                            }
                        )
                    }

                    editPastPaper?.let { paper ->
                        AddPastPaperDialog(
                            subjects = subjects,
                            onDismiss = { editPastPaper = null },
                            onSave = { subId, year, term, score, time, mistakes, comp ->
                                viewModel.updatePastPaper(
                                    paper.copy(
                                        subjectId = subId,
                                        year = year,
                                        term = term,
                                        score = score,
                                        timeTakenMinutes = time,
                                        mistakes = mistakes,
                                        isCompleted = comp
                                    )
                                )
                                editPastPaper = null
                            }
                        )
                    }

                    if (showAddNote) {
                        AddStudyNoteDialog(
                            subjects = subjects,
                            onDismiss = { showAddNote = false },
                            onSave = { subId, topic, summary, formulas, facts, keywords ->
                                viewModel.addNote(subId, topic, summary, facts, formulas, keywords, "", false)
                                showAddNote = false
                            }
                        )
                    }

                    editNote?.let { n ->
                        AddStudyNoteDialog(
                            subjects = subjects,
                            onDismiss = { editNote = null },
                            onSave = { subId, topic, summary, formulas, facts, keywords ->
                                viewModel.updateNote(
                                    n.copy(
                                        subjectId = subId,
                                        topic = topic,
                                        summary = summary,
                                        formulas = formulas,
                                        importantFacts = facts,
                                        keywords = keywords
                                    )
                                )
                                editNote = null
                            }
                        )
                    }

                    if (showAddMindMap) {
                        AddMindMapDialog(
                            subjects = subjects,
                            onDismiss = { showAddMindMap = false },
                            onSave = { subId, title, mainIdea, branches, keywords ->
                                viewModel.addMindMap(subId, title, mainIdea, branches, "", keywords)
                                showAddMindMap = false
                            }
                        )
                    }

                    editMindMap?.let { m ->
                        AddMindMapDialog(
                            subjects = subjects,
                            onDismiss = { editMindMap = null },
                            onSave = { subId, title, mainIdea, branches, keywords ->
                                viewModel.updateMindMap(
                                    m.copy(
                                        subjectId = subId,
                                        title = title,
                                        mainIdea = mainIdea,
                                        branchesText = branches,
                                        importantKeywords = keywords
                                    )
                                )
                                editMindMap = null
                            }
                        )
                    }

                    if (showAddWeakArea) {
                        AddWeakAreaDialog(
                            subjects = subjects,
                            onDismiss = { showAddWeakArea = false },
                            onSave = { subId, topic, type, severity, notes ->
                                viewModel.addWeakArea(subId, topic, type, severity, notes)
                                showAddWeakArea = false
                            }
                        )
                    }

                    if (showEditStudyBlocks) {
                        EditStudyBlocksDialog(
                            studyBlocks = studyBlocks,
                            onDismiss = { showEditStudyBlocks = false },
                            onSaveBlock = { block -> viewModel.updateStudyBlock(block) }
                        )
                    }

                    if (showSettingsDialog) {
                        SettingsDialog(
                            userSettings = userSettings,
                            onDismiss = { showSettingsDialog = false },
                            onUpdateDailyGoal = { goalMins -> viewModel.updateDailyGoal(goalMins) }
                        )
                    }

                    inspectedSubject?.let { sub ->
                        SubjectDetailDialog(
                            subject = sub,
                            viewModel = viewModel,
                            onDismiss = { inspectedSubject = null },
                            onStartTimer = { subId -> viewModel.openTimerDialog(subId) }
                        )
                    }
                }
            }
        }
    }
}
