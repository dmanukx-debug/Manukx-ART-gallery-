package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StudyTab {
    HOME,
    PLANNER,
    SUBJECTS,
    PROGRESS,
    REVISION
}

data class WeeklySummary(
    val weekNumber: Int,
    val weekTitle: String,
    val dayRange: String,
    val subjectsStudied: List<String>,
    val hoursCompleted: Float,
    val totalHoursPlanned: Float,
    val tasksCompleted: Int,
    val totalTasks: Int,
    val testBooksCompleted: Int,
    val pastPapersCompleted: Int,
    val notesCompleted: Int,
    val mindMapsCompleted: Int,
    val remainingTasks: Int
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = StudyRepository(db.studyDao())
        viewModelScope.launch {
            repository.ensureDataInitialized()
        }
    }

    // --- Navigation & Core UI State ---
    private val _currentTab = MutableStateFlow(StudyTab.HOME)
    val currentTab: StateFlow<StudyTab> = _currentTab.asStateFlow()

    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<String?>("math")
    val selectedSubjectId: StateFlow<String?> = _selectedSubjectId.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // --- Room Database Flows ---
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayPlans: StateFlow<List<DayPlanEntity>> = repository.allDayPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testBooks: StateFlow<List<TestBookEntity>> = repository.allTestBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastPapers: StateFlow<List<PastPaperEntity>> = repository.allPastPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<StudyNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mindMaps: StateFlow<List<MindMapEntity>> = repository.allMindMaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weakAreas: StateFlow<List<WeakAreaEntity>> = repository.allWeakAreas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyBlocks: StateFlow<List<StudyBlockEntity>> = repository.allStudyBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Study Session Timer ---
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _currentBlockIndex = MutableStateFlow(0)
    val currentBlockIndex: StateFlow<Int> = _currentBlockIndex.asStateFlow()

    private val _timerSubjectId = MutableStateFlow("math")
    val timerSubjectId: StateFlow<String> = _timerSubjectId.asStateFlow()

    private val _showTimerDialog = MutableStateFlow(false)
    val showTimerDialog: StateFlow<Boolean> = _showTimerDialog.asStateFlow()

    private var timerJob: Job? = null

    // --- Computed Derived States ---
    val totalTargetHours = 60

    val totalCompletedMinutes: StateFlow<Int> = combine(subjects) { subs ->
        subs.first().sumOf { it.completedMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overallProgressPercent: StateFlow<Int> = totalCompletedMinutes.map { minutes ->
        val totalMins = 60 * 60
        ((minutes.toDouble() / totalMins) * 100).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val daysRemaining: StateFlow<Int> = combine(userSettings) { settings ->
        val examMillis = settings.first()?.examDateMillis ?: (System.currentTimeMillis() + 30L * 24 * 3600 * 1000)
        val diff = examMillis - System.currentTimeMillis()
        val days = (diff / (1000L * 60 * 60 * 24)).toInt()
        days.coerceIn(0, 30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    // --- Actions ---
    fun selectTab(tab: StudyTab) {
        _currentTab.value = tab
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day.coerceIn(1, 30)
    }

    fun selectSubject(subjectId: String?) {
        _selectedSubjectId.value = subjectId
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            val updated = current.copy(isDarkTheme = !current.isDarkTheme)
            repository.updateUserSettings(updated)
        }
    }

    fun updateExamDate(dateMillis: Long) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(examDateMillis = dateMillis))
            _toastMessage.emit("Exam date updated!")
        }
    }

    fun updateDailyGoal(minutes: Int) {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            repository.updateUserSettings(current.copy(dailyGoalMinutes = minutes))
            _toastMessage.emit("Daily study goal updated to ${minutes / 60}h ${minutes % 60}m")
        }
    }

    // --- Task Actions ---
    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
            checkAchievementsState()
        }
    }

    fun addTask(
        dayNumber: Int,
        subjectId: String,
        title: String,
        category: String,
        minutes: Int,
        priority: String,
        difficulty: String,
        isWeakTopic: Boolean
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                dayNumber = dayNumber,
                subjectId = subjectId,
                title = title,
                category = category,
                plannedMinutes = minutes,
                completedMinutes = 0,
                isCompleted = false,
                priority = priority,
                difficulty = difficulty,
                isWeakTopic = isWeakTopic
            )
            repository.insertTask(task)
            _toastMessage.emit("Task added for Day $dayNumber")
            checkAchievementsState()
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            checkAchievementsState()
        }
    }

    fun updateDayPlan(plan: DayPlanEntity) {
        viewModelScope.launch {
            repository.updateDayPlan(plan)
            _toastMessage.emit("Day ${plan.dayNumber} goal updated")
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _toastMessage.emit("Task deleted")
        }
    }

    // --- Test Book Actions ---
    fun updateTestBook(book: TestBookEntity) {
        viewModelScope.launch {
            repository.updateTestBook(book)
            _toastMessage.emit("Test book updated")
            checkAchievementsState()
        }
    }

    // --- Past Paper Actions ---
    fun addPastPaper(
        subjectId: String,
        year: String,
        term: String,
        score: Int,
        maxScore: Int,
        timeTaken: Int,
        mistakes: String,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            val paper = PastPaperEntity(
                subjectId = subjectId,
                year = year,
                term = term,
                score = score,
                maxScore = maxScore,
                timeTakenMinutes = timeTaken,
                mistakes = mistakes,
                isCompleted = isCompleted,
                completedDate = System.currentTimeMillis()
            )
            repository.insertPastPaper(paper)
            _toastMessage.emit("Past paper record saved")
            checkAchievementsState()
        }
    }

    fun updatePastPaper(paper: PastPaperEntity) {
        viewModelScope.launch {
            repository.updatePastPaper(paper)
            checkAchievementsState()
        }
    }

    fun deletePastPaper(paper: PastPaperEntity) {
        viewModelScope.launch {
            repository.deletePastPaper(paper)
            _toastMessage.emit("Past paper removed")
        }
    }

    // --- Notes Actions ---
    fun addNote(
        subjectId: String,
        topic: String,
        summary: String,
        importantFacts: String,
        formulas: String,
        keywords: String,
        questions: String,
        isWeakTopic: Boolean
    ) {
        viewModelScope.launch {
            val note = StudyNoteEntity(
                subjectId = subjectId,
                topic = topic,
                summary = summary,
                importantFacts = importantFacts,
                formulas = formulas,
                keywords = keywords,
                questionsToRemember = questions,
                isWeakTopic = isWeakTopic
            )
            repository.insertNote(note)
            _toastMessage.emit("Study note created")
            checkAchievementsState()
        }
    }

    fun updateNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            _toastMessage.emit("Study note updated")
        }
    }

    fun deleteNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            _toastMessage.emit("Study note deleted")
        }
    }

    // --- Mind Map Actions ---
    fun addMindMap(
        subjectId: String,
        title: String,
        mainIdea: String,
        branchesText: String,
        subtopics: String,
        keywords: String
    ) {
        viewModelScope.launch {
            val mindMap = MindMapEntity(
                subjectId = subjectId,
                title = title,
                mainIdea = mainIdea,
                branchesText = branchesText,
                subtopics = subtopics,
                importantKeywords = keywords
            )
            repository.insertMindMap(mindMap)
            _toastMessage.emit("Mind map created")
            checkAchievementsState()
        }
    }

    fun updateMindMap(mindMap: MindMapEntity) {
        viewModelScope.launch {
            repository.updateMindMap(mindMap)
            _toastMessage.emit("Mind map updated")
        }
    }

    fun deleteMindMap(mindMap: MindMapEntity) {
        viewModelScope.launch {
            repository.deleteMindMap(mindMap)
            _toastMessage.emit("Mind map deleted")
        }
    }

    // --- Weak Areas Actions ---
    fun addWeakArea(
        subjectId: String,
        topic: String,
        type: String,
        severity: String,
        notes: String
    ) {
        viewModelScope.launch {
            val area = WeakAreaEntity(
                subjectId = subjectId,
                topic = topic,
                type = type,
                severity = severity,
                notes = notes,
                isResolved = false
            )
            repository.insertWeakArea(area)
            _toastMessage.emit("Added to Revision Center")
        }
    }

    fun resolveWeakArea(area: WeakAreaEntity) {
        viewModelScope.launch {
            repository.updateWeakArea(area.copy(isResolved = !area.isResolved))
            _toastMessage.emit(if (!area.isResolved) "Resolved! Great progress." else "Marked as active revision.")
        }
    }

    fun deleteWeakArea(area: WeakAreaEntity) {
        viewModelScope.launch {
            repository.deleteWeakArea(area)
            _toastMessage.emit("Revision item removed")
        }
    }

    // --- Study Timer Actions (2-Hour Study Structure) ---
    fun openTimerDialog(subjectId: String = "math") {
        _timerSubjectId.value = subjectId
        _showTimerDialog.value = true
        if (!_isTimerRunning.value) {
            val blocks = studyBlocks.value
            val initialDuration = if (blocks.isNotEmpty()) blocks[0].durationMinutes * 60 else 25 * 60
            _timerRemainingSeconds.value = initialDuration
            _currentBlockIndex.value = 0
        }
    }

    fun closeTimerDialog() {
        _showTimerDialog.value = false
    }

    fun setTimerSubject(subjectId: String) {
        _timerSubjectId.value = subjectId
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerRemainingSeconds.value > 0) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }
            if (_timerRemainingSeconds.value <= 0) {
                // Advance block
                advanceTimerBlock()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimerCurrentBlock() {
        pauseTimer()
        val blocks = studyBlocks.value
        val blockIdx = _currentBlockIndex.value
        val duration = if (blocks.indices.contains(blockIdx)) blocks[blockIdx].durationMinutes * 60 else 25 * 60
        _timerRemainingSeconds.value = duration
    }

    fun advanceTimerBlock() {
        pauseTimer()
        val blocks = studyBlocks.value
        val nextIdx = (_currentBlockIndex.value + 1) % if (blocks.isNotEmpty()) blocks.size else 7
        _currentBlockIndex.value = nextIdx
        val duration = if (blocks.indices.contains(nextIdx)) blocks[nextIdx].durationMinutes * 60 else 25 * 60
        _timerRemainingSeconds.value = duration
    }

    fun finishSessionAndLog(minutes: Int) {
        viewModelScope.launch {
            val day = selectedDay.value
            val subId = timerSubjectId.value
            repository.recordCompletedSession(day, subId, minutes)
            _toastMessage.emit("Logged $minutes mins study session to Day $day!")
            pauseTimer()
            checkAchievementsState()
        }
    }

    fun updateStudyBlock(block: StudyBlockEntity) {
        viewModelScope.launch {
            repository.updateStudyBlock(block)
            _toastMessage.emit("Study block updated")
        }
    }

    // --- Streak & Achievements Verification ---
    private fun checkAchievementsState() {
        viewModelScope.launch {
            val allAchievements = achievements.value
            val allBooks = testBooks.value
            val allNotes = notes.value
            val allMaps = mindMaps.value
            val allTasks = tasks.value
            val currentSettings = userSettings.value

            val completedBooks = allBooks.count { it.completedQuestions >= it.totalQuestions && it.totalQuestions > 0 }
            val completedNotes = allNotes.size
            val completedMaps = allMaps.size
            val streak = currentSettings?.streakCount ?: 0
            val mockCompleted = allTasks.any { it.category == "MOCK_TEST" && it.isCompleted }

            allAchievements.forEach { ach ->
                val shouldUnlock = when (ach.id) {
                    "streak_3" -> streak >= 3
                    "streak_7" -> streak >= 7
                    "streak_10" -> streak >= 10
                    "testbook_1" -> completedBooks >= 1
                    "testbook_5" -> completedBooks >= 5
                    "testbook_10" -> completedBooks >= 10
                    "mindmap_10" -> completedMaps >= 10
                    "notes_20" -> completedNotes >= 20
                    "mocktest_1" -> mockCompleted
                    else -> false
                }
                if (shouldUnlock && !ach.isUnlocked) {
                    val updated = ach.copy(isUnlocked = true, unlockedDate = System.currentTimeMillis())
                    repository.updateUserSettings(currentSettings ?: UserSettingsEntity())
                    // unlock in db
                    AppDatabase.getDatabase(getApplication(), viewModelScope).studyDao().updateAchievement(updated)
                    _toastMessage.emit("Achievement Unlocked: ${ach.title}!")
                }
            }
        }
    }

    fun incrementStreakManual() {
        viewModelScope.launch {
            val current = userSettings.value ?: UserSettingsEntity()
            val updated = current.copy(
                streakCount = current.streakCount + 1,
                lastStudyDateMillis = System.currentTimeMillis()
            )
            repository.updateUserSettings(updated)
            _toastMessage.emit("Study Streak increased to ${updated.streakCount} days! Keep it up!")
            checkAchievementsState()
        }
    }
}
