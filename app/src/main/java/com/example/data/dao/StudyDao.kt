package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Subjects ---
    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Query("SELECT * FROM subjects ORDER BY targetHours DESC, name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("UPDATE subjects SET completedMinutes = completedMinutes + :minutes WHERE id = :subjectId")
    suspend fun addSubjectMinutes(subjectId: String, minutes: Int)

    // --- Day Plans ---
    @Query("SELECT * FROM day_plans ORDER BY dayNumber ASC")
    fun getAllDayPlans(): Flow<List<DayPlanEntity>>

    @Query("SELECT * FROM day_plans WHERE dayNumber = :dayNumber LIMIT 1")
    fun getDayPlan(dayNumber: Int): Flow<DayPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayPlans(plans: List<DayPlanEntity>)

    @Update
    suspend fun updateDayPlan(plan: DayPlanEntity)

    @Query("UPDATE day_plans SET completedMinutes = completedMinutes + :minutes WHERE dayNumber = :dayNumber")
    suspend fun addDayMinutes(dayNumber: Int, minutes: Int)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY dayNumber ASC, id ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dayNumber = :dayNumber ORDER BY id ASC")
    fun getTasksForDay(dayNumber: Int): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    // --- Test Books ---
    @Query("SELECT * FROM test_books ORDER BY bookNumber ASC")
    fun getAllTestBooks(): Flow<List<TestBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestBooks(books: List<TestBookEntity>)

    @Update
    suspend fun updateTestBook(book: TestBookEntity)

    // --- Past Papers ---
    @Query("SELECT * FROM past_papers ORDER BY completedDate DESC, id DESC")
    fun getAllPastPapers(): Flow<List<PastPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastPaper(paper: PastPaperEntity): Long

    @Update
    suspend fun updatePastPaper(paper: PastPaperEntity)

    @Delete
    suspend fun deletePastPaper(paper: PastPaperEntity)

    // --- Study Notes ---
    @Query("SELECT * FROM study_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Delete
    suspend fun deleteNote(note: StudyNoteEntity)

    // --- Mind Maps ---
    @Query("SELECT * FROM mind_maps ORDER BY createdAt DESC")
    fun getAllMindMaps(): Flow<List<MindMapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindMap(mindMap: MindMapEntity): Long

    @Update
    suspend fun updateMindMap(mindMap: MindMapEntity)

    @Delete
    suspend fun deleteMindMap(mindMap: MindMapEntity)

    // --- Weak Areas & Mistakes ---
    @Query("SELECT * FROM weak_areas ORDER BY isResolved ASC, severity DESC, id DESC")
    fun getAllWeakAreas(): Flow<List<WeakAreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeakArea(weakArea: WeakAreaEntity): Long

    @Update
    suspend fun updateWeakArea(weakArea: WeakAreaEntity)

    @Delete
    suspend fun deleteWeakArea(weakArea: WeakAreaEntity)

    // --- Daily Study Structure Blocks ---
    @Query("SELECT * FROM study_blocks ORDER BY orderIndex ASC")
    fun getAllStudyBlocks(): Flow<List<StudyBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyBlocks(blocks: List<StudyBlockEntity>)

    @Update
    suspend fun updateStudyBlock(block: StudyBlockEntity)

    // --- User Settings ---
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateUserSettings(settings: UserSettingsEntity)

    // --- Achievements ---
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}
