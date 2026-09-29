package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.StudyDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        DayPlanEntity::class,
        TaskEntity::class,
        TestBookEntity::class,
        PastPaperEntity::class,
        StudyNoteEntity::class,
        MindMapEntity::class,
        WeakAreaEntity::class,
        StudyBlockEntity::class,
        UserSettingsEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_planner.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialStudyPlan(database.studyDao())
                    }
                }
            }
        }

        suspend fun populateInitialStudyPlan(dao: StudyDao) {
                // 1. Ten Grade 8 Subjects intelligently divided (60 Hours total)
                val subjects = listOf(
                    SubjectEntity(
                        id = "math",
                        name = "Mathematics",
                        category = "Heavy",
                        colorHex = 0xFF4F46E5, // Indigo
                        iconName = "calculate",
                        targetHours = 8,
                        completedMinutes = 0,
                        priority = "High"
                    ),
                    SubjectEntity(
                        id = "science",
                        name = "Science",
                        category = "Heavy",
                        colorHex = 0xFF10B981, // Emerald
                        iconName = "science",
                        targetHours = 8,
                        completedMinutes = 0,
                        priority = "High"
                    ),
                    SubjectEntity(
                        id = "sinhala",
                        name = "Sinhala",
                        category = "Language",
                        colorHex = 0xFF8B5CF6, // Violet
                        iconName = "translate",
                        targetHours = 6,
                        completedMinutes = 0,
                        priority = "Medium"
                    ),
                    SubjectEntity(
                        id = "english",
                        name = "English",
                        category = "Language",
                        colorHex = 0xFF0EA5E9, // Sky Blue
                        iconName = "menu_book",
                        targetHours = 6,
                        completedMinutes = 0,
                        priority = "Medium"
                    ),
                    SubjectEntity(
                        id = "history",
                        name = "History",
                        category = "Theory",
                        colorHex = 0xFFD97706, // Amber
                        iconName = "history_edu",
                        targetHours = 6,
                        completedMinutes = 0,
                        priority = "Medium"
                    ),
                    SubjectEntity(
                        id = "geography",
                        name = "Geography",
                        category = "Theory",
                        colorHex = 0xFF14B8A6, // Teal
                        iconName = "public",
                        targetHours = 6,
                        completedMinutes = 0,
                        priority = "Medium"
                    ),
                    SubjectEntity(
                        id = "buddhism",
                        name = "Buddhist Studies",
                        category = "Theory",
                        colorHex = 0xFFF97316, // Orange
                        iconName = "self_improvement",
                        targetHours = 5,
                        completedMinutes = 0,
                        priority = "Normal"
                    ),
                    SubjectEntity(
                        id = "civic",
                        name = "Civic Education",
                        category = "Theory",
                        colorHex = 0xFFF43F5E, // Rose
                        iconName = "account_balance",
                        targetHours = 5,
                        completedMinutes = 0,
                        priority = "Normal"
                    ),
                    SubjectEntity(
                        id = "health",
                        name = "Health",
                        category = "Theory",
                        colorHex = 0xFF84CC16, // Lime
                        iconName = "favorite",
                        targetHours = 5,
                        completedMinutes = 0,
                        priority = "Normal"
                    ),
                    SubjectEntity(
                        id = "art",
                        name = "Art",
                        category = "Creative",
                        colorHex = 0xFFEC4899, // Pink
                        iconName = "palette",
                        targetHours = 5,
                        completedMinutes = 0,
                        priority = "Normal"
                    )
                )
                dao.insertSubjects(subjects)

                // 2. 30 Days Plan across 4 Phases with smart subject rotation
                val rotations = listOf(
                    Pair("math", "english"),
                    Pair("science", "sinhala"),
                    Pair("history", "art"),
                    Pair("geography", "health"),
                    Pair("buddhism", "civic"),
                    Pair("math", "sinhala"),
                    Pair("science", "english"),
                    Pair("history", "geography"),
                    Pair("math", "buddhism"),
                    Pair("science", "health"),
                    Pair("math", "civic"),
                    Pair("science", "art"),
                    Pair("sinhala", "history"),
                    Pair("english", "geography"),
                    Pair("buddhism", "health"),
                    Pair("math", "science"),
                    Pair("sinhala", "english"),
                    Pair("history", "civic"),
                    Pair("geography", "health"),
                    Pair("buddhism", "art"),
                    Pair("math", "history"),
                    Pair("science", "geography"),
                    Pair("english", "civic"),
                    Pair("sinhala", "health"),
                    Pair("math", "art"),
                    Pair("science", "buddhism"),
                    Pair("math", "sinhala"),
                    Pair("science", "english"),
                    Pair("history", "geography"),
                    Pair("buddhism", "civic")
                )

                val dayGoals = listOf(
                    "Master algebra foundation & English tenses",
                    "Understand plant/animal cells & Sinhala grammar rules",
                    "Timeline of Sri Lankan kingdoms & basic color sketching",
                    "Sri Lankan topography & balanced dietary guidelines",
                    "The Four Noble Truths & Democratic principles overview",
                    "Fractions and decimals drill & Sinhala essay structures",
                    "Matter and elements review & English reading comprehension",
                    "Ancient hydraulic technology & Sri Lanka climate zones",
                    "Geometric angles, perimeter & Buddhist moral philosophy",
                    "Force, motion & human reproductive health awareness",
                    "Equations and algebraic terms & Fundamental human rights",
                    "Chemical reactions basics & Design principles & perspective",
                    "Classical poetry analysis & Anuradhapura civilization",
                    "Formal letter writing & World maps & landforms",
                    "Buddhist monastic order history & First aid and safety",
                    "Mathematics Past Paper Section A & Science structured questions",
                    "Sinhala literature questions & English model test paper 1",
                    "History structured essay questions & Local government duties",
                    "Geography topographic mapping & Mental health and wellness",
                    "Buddhist heritage monuments & Traditional Sri Lankan art motifs",
                    "Mathematics word problems revision & European colonial period",
                    "Science physics formulas & Contour map interpretation",
                    "English comprehension & Constitution and rule of law",
                    "Sinhala spelling & grammar drill & Disease prevention methods",
                    "Mathematics volume & surface area & Creative composition",
                    "Science biology classification & Buddhist ethics & society",
                    "Full Mathematics Mock Paper 1 & Sinhala model test 2",
                    "Full Science Mock Paper 1 & English speed essay writing",
                    "History key dates rapid recap & Geography map markings",
                    "Complete final revision of short notes, formulas & mind maps"
                )

                val dayPlans = (1..30).map { day ->
                    val phase = when {
                        day <= 10 -> 1
                        day <= 20 -> 2
                        day <= 26 -> 3
                        else -> 4
                    }
                    val phaseTitle = when (phase) {
                        1 -> "PHASE 1: Foundation & Understanding"
                        2 -> "PHASE 2: Practice & Test Books"
                        3 -> "PHASE 3: Past Papers & Intensive Revision"
                        else -> "PHASE 4: Final Revision & Mock Tests"
                    }
                    val weekNumber = when {
                        day <= 7 -> 1
                        day <= 15 -> 2
                        day <= 22 -> 3
                        else -> 4
                    }
                    val rotation = rotations[day - 1]
                    DayPlanEntity(
                        dayNumber = day,
                        phase = phase,
                        phaseTitle = phaseTitle,
                        weekNumber = weekNumber,
                        subject1Id = rotation.first,
                        subject2Id = rotation.second,
                        goal = dayGoals[day - 1],
                        plannedMinutes = 120,
                        completedMinutes = 0,
                        isCompleted = false,
                        notes = ""
                    )
                }
                dao.insertDayPlans(dayPlans)

                // 3. Populate default 2-hour structure tasks for each day (all starting incomplete at 0 mins)
                val defaultTasks = mutableListOf<TaskEntity>()
                dayPlans.forEach { plan ->
                    defaultTasks.add(
                        TaskEntity(
                            dayNumber = plan.dayNumber,
                            subjectId = plan.subject1Id,
                            title = "25m: Theory & Key Lessons",
                            category = "STUDY",
                            plannedMinutes = 25,
                            completedMinutes = 0,
                            isCompleted = false,
                            priority = "HIGH",
                            difficulty = "MEDIUM"
                        )
                    )
                    defaultTasks.add(
                        TaskEntity(
                            dayNumber = plan.dayNumber,
                            subjectId = plan.subject1Id,
                            title = "25m: Questions & Textbook Exercises",
                            category = "TEST_BOOK",
                            plannedMinutes = 25,
                            completedMinutes = 0,
                            isCompleted = false,
                            priority = "MEDIUM",
                            difficulty = "MEDIUM"
                        )
                    )
                    defaultTasks.add(
                        TaskEntity(
                            dayNumber = plan.dayNumber,
                            subjectId = plan.subject2Id,
                            title = "25m: Short Notes & Mind Map Creation",
                            category = "SHORT_NOTE",
                            plannedMinutes = 25,
                            completedMinutes = 0,
                            isCompleted = false,
                            priority = "MEDIUM",
                            difficulty = "EASY"
                        )
                    )
                    defaultTasks.add(
                        TaskEntity(
                            dayNumber = plan.dayNumber,
                            subjectId = plan.subject2Id,
                            title = "25m: Past Paper / Test Questions Drill",
                            category = "PAST_PAPER",
                            plannedMinutes = 25,
                            completedMinutes = 0,
                            isCompleted = false,
                            priority = "HIGH",
                            difficulty = "HARD"
                        )
                    )
                    defaultTasks.add(
                        TaskEntity(
                            dayNumber = plan.dayNumber,
                            subjectId = plan.subject1Id,
                            title = "15m: Review Mistakes & Difficult Concepts",
                            category = "MISTAKE_CORRECTION",
                            plannedMinutes = 15,
                            completedMinutes = 0,
                            isCompleted = false,
                            priority = "MEDIUM",
                            difficulty = "HARD"
                        )
                    )
                }
                dao.insertTasks(defaultTasks)

                // 4. Ten Full-Test Books (one for each subject, progress starts at 0)
                val testBooks = listOf(
                    TestBookEntity(
                        bookNumber = 1,
                        title = "Mathematics Master Full-Test Book",
                        subjectId = "math",
                        totalQuestions = 100,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 2,
                        title = "Science Term Test Practice Book",
                        subjectId = "science",
                        totalQuestions = 100,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 3,
                        title = "Sinhala Language & Model Papers Book",
                        subjectId = "sinhala",
                        totalQuestions = 80,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 4,
                        title = "English Comprehensive Test Series",
                        subjectId = "english",
                        totalQuestions = 80,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 5,
                        title = "Buddhist Studies Evaluation Test Book",
                        subjectId = "buddhism",
                        totalQuestions = 80,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 6,
                        title = "History Chronology & Test Questions",
                        subjectId = "history",
                        totalQuestions = 80,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 7,
                        title = "Geography Map Work & Test Papers",
                        subjectId = "geography",
                        totalQuestions = 80,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 8,
                        title = "Civic Education Complete Test Book",
                        subjectId = "civic",
                        totalQuestions = 70,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 9,
                        title = "Health & Physical Science Test Workbook",
                        subjectId = "health",
                        totalQuestions = 70,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    ),
                    TestBookEntity(
                        bookNumber = 10,
                        title = "Art & Aesthetic Appreciation Test Book",
                        subjectId = "art",
                        totalQuestions = 60,
                        completedQuestions = 0,
                        scorePercentage = 0,
                        mistakesCount = 0,
                        revisionStatus = "NOT_STARTED"
                    )
                )
                dao.insertTestBooks(testBooks)

                // 5. Daily 2-Hour Study Structure Blocks (Editable)
                val studyBlocks = listOf(
                    StudyBlockEntity(orderIndex = 1, title = "Main lesson / theory", durationMinutes = 25, isBreak = false),
                    StudyBlockEntity(orderIndex = 2, title = "Short break & hydrate", durationMinutes = 5, isBreak = true),
                    StudyBlockEntity(orderIndex = 3, title = "Questions / exercises", durationMinutes = 25, isBreak = false),
                    StudyBlockEntity(orderIndex = 4, title = "Short break & stretch", durationMinutes = 5, isBreak = true),
                    StudyBlockEntity(orderIndex = 5, title = "Short notes or mind map", durationMinutes = 25, isBreak = false),
                    StudyBlockEntity(orderIndex = 6, title = "Short break & relax eyes", durationMinutes = 5, isBreak = true),
                    StudyBlockEntity(orderIndex = 7, title = "Past paper / test questions", durationMinutes = 25, isBreak = false)
                )
                dao.insertStudyBlocks(studyBlocks)

                // 6. User Settings
                dao.insertUserSettings(
                    UserSettingsEntity(
                        id = 1,
                        streakCount = 0,
                        lastStudyDateMillis = 0L,
                        examDateMillis = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                        totalAvailableHours = 60,
                        dailyGoalMinutes = 120,
                        isDarkTheme = true
                    )
                )

                // 7. Gamification Achievements (clean, premium educational badges)
                val achievements = listOf(
                    AchievementEntity(id = "streak_3", title = "3-Day Streak", description = "Studied 3 consecutive days without missing", iconEmoji = "🔥", isUnlocked = false),
                    AchievementEntity(id = "streak_7", title = "7-Day Streak", description = "Maintained a strong 1-week study consistency", iconEmoji = "🔥", isUnlocked = false),
                    AchievementEntity(id = "streak_10", title = "10-Day Streak", description = "Reached double digits in study commitment", iconEmoji = "🔥", isUnlocked = false),
                    AchievementEntity(id = "testbook_1", title = "First Test Book Done", description = "Finished 1 full subject test book", iconEmoji = "🏆", isUnlocked = false),
                    AchievementEntity(id = "testbook_5", title = "Halfway Master", description = "Completed 5 full test books", iconEmoji = "🏆", isUnlocked = false),
                    AchievementEntity(id = "testbook_10", title = "All Test Books Finished", description = "Completed all 10 full test books across all subjects", iconEmoji = "🏆", isUnlocked = false),
                    AchievementEntity(id = "mindmap_10", title = "Mind Map Master", description = "Crafted 10 detailed visual mind maps", iconEmoji = "🧠", isUnlocked = false),
                    AchievementEntity(id = "notes_20", title = "Knowledge Vault", description = "Created 20 structured short revision notes", iconEmoji = "📝", isUnlocked = false),
                    AchievementEntity(id = "mocktest_1", title = "Mock Exam Pioneer", description = "Completed first full timed mock test", iconEmoji = "🎯", isUnlocked = false)
                )
                dao.insertAchievements(achievements)
            }
    }
}
