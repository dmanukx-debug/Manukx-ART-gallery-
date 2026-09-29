package com.example.data.repository

import com.example.data.dao.StudyDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class StudyRepository(private val studyDao: StudyDao) {

    val allSubjects: Flow<List<SubjectEntity>> = studyDao.getAllSubjects()
    val allDayPlans: Flow<List<DayPlanEntity>> = studyDao.getAllDayPlans()
    val allTasks: Flow<List<TaskEntity>> = studyDao.getAllTasks()
    val allTestBooks: Flow<List<TestBookEntity>> = studyDao.getAllTestBooks()
    val allPastPapers: Flow<List<PastPaperEntity>> = studyDao.getAllPastPapers()
    val allNotes: Flow<List<StudyNoteEntity>> = studyDao.getAllNotes()
    val allMindMaps: Flow<List<MindMapEntity>> = studyDao.getAllMindMaps()
    val allWeakAreas: Flow<List<WeakAreaEntity>> = studyDao.getAllWeakAreas()
    val allStudyBlocks: Flow<List<StudyBlockEntity>> = studyDao.getAllStudyBlocks()
    val userSettings: Flow<UserSettingsEntity?> = studyDao.getUserSettings()
    val allAchievements: Flow<List<AchievementEntity>> = studyDao.getAllAchievements()

    fun getTasksForDay(dayNumber: Int): Flow<List<TaskEntity>> = studyDao.getTasksForDay(dayNumber)
    fun getDayPlan(dayNumber: Int): Flow<DayPlanEntity?> = studyDao.getDayPlan(dayNumber)

    suspend fun ensureDataInitialized() {
        val count = studyDao.getSubjectCount()
        if (count == 0) {
            com.example.data.database.AppDatabase.populateInitialStudyPlan(studyDao)
        }
    }

    // --- Task Actions ---
    suspend fun insertTask(task: TaskEntity) = studyDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = studyDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = studyDao.deleteTask(task)

    suspend fun toggleTaskCompleted(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedMinutes = if (!task.isCompleted) task.plannedMinutes else 0
        )
        studyDao.updateTask(updated)

        // Automatically create a weak area entry if marked as weak topic
        if (task.isWeakTopic && !task.isCompleted) {
            studyDao.insertWeakArea(
                WeakAreaEntity(
                    subjectId = task.subjectId,
                    topic = task.title,
                    type = "WEAK_TOPIC",
                    severity = if (task.difficulty == "HARD") "HIGH" else "MEDIUM",
                    notes = "Added from Daily Task: Day ${task.dayNumber}",
                    isResolved = false
                )
            )
        }
    }

    // --- Study Session Completion & Timer ---
    suspend fun recordCompletedSession(dayNumber: Int, subjectId: String, minutes: Int) {
        if (minutes <= 0) return
        studyDao.addDayMinutes(dayNumber, minutes)
        studyDao.addSubjectMinutes(subjectId, minutes)

        // Update streak logic
        val now = System.currentTimeMillis()
        val currentCalendar = Calendar.getInstance().apply { timeInMillis = now }

        val todayYear = currentCalendar.get(Calendar.YEAR)
        val todayDayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR)

        // We can check and advance streak
        // Also evaluate achievements
        checkAndUpdateAchievements()
    }

    // --- Subject Actions ---
    suspend fun updateSubject(subject: SubjectEntity) = studyDao.updateSubject(subject)

    // --- Day Plan Actions ---
    suspend fun updateDayPlan(plan: DayPlanEntity) = studyDao.updateDayPlan(plan)

    // --- Test Book Actions ---
    suspend fun updateTestBook(book: TestBookEntity) {
        studyDao.updateTestBook(book)
        checkAndUpdateAchievements()
    }

    // --- Past Paper Actions ---
    suspend fun insertPastPaper(paper: PastPaperEntity): Long {
        val id = studyDao.insertPastPaper(paper)
        checkAndUpdateAchievements()
        return id
    }

    suspend fun updatePastPaper(paper: PastPaperEntity) {
        studyDao.updatePastPaper(paper)
        checkAndUpdateAchievements()
    }

    suspend fun deletePastPaper(paper: PastPaperEntity) = studyDao.deletePastPaper(paper)

    // --- Notes Actions ---
    suspend fun insertNote(note: StudyNoteEntity): Long {
        val id = studyDao.insertNote(note)
        checkAndUpdateAchievements()
        return id
    }

    suspend fun updateNote(note: StudyNoteEntity) = studyDao.updateNote(note)
    suspend fun deleteNote(note: StudyNoteEntity) = studyDao.deleteNote(note)

    // --- Mind Map Actions ---
    suspend fun insertMindMap(mindMap: MindMapEntity): Long {
        val id = studyDao.insertMindMap(mindMap)
        checkAndUpdateAchievements()
        return id
    }

    suspend fun updateMindMap(mindMap: MindMapEntity) = studyDao.updateMindMap(mindMap)
    suspend fun deleteMindMap(mindMap: MindMapEntity) = studyDao.deleteMindMap(mindMap)

    // --- Weak Area Actions ---
    suspend fun insertWeakArea(area: WeakAreaEntity): Long = studyDao.insertWeakArea(area)
    suspend fun updateWeakArea(area: WeakAreaEntity) = studyDao.updateWeakArea(area)
    suspend fun deleteWeakArea(area: WeakAreaEntity) = studyDao.deleteWeakArea(area)

    // --- Study Blocks ---
    suspend fun updateStudyBlock(block: StudyBlockEntity) = studyDao.updateStudyBlock(block)

    // --- Settings & Streak ---
    suspend fun updateUserSettings(settings: UserSettingsEntity) = studyDao.updateUserSettings(settings)

    suspend fun incrementStreak() {
        // Can be invoked manually or upon completing day goal
    }

    suspend fun checkAndUpdateAchievements() {
        // Evaluate dynamic conditions
    }
}
