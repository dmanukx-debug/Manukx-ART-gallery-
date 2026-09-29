package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The 10 Grade 8 subjects with intelligent time allocations totaling 60 hours.
 */
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String, // e.g. "math", "science", "sinhala", etc.
    val name: String,
    val category: String, // "Heavy", "Language", "Theory", "Creative"
    val colorHex: Long,
    val iconName: String,
    val targetHours: Int, // e.g. Math: 8, Science: 8, English: 6, Sinhala: 6, History: 6, Geography: 6, Buddhism: 5, Civic: 5, Health: 5, Art: 5
    val completedMinutes: Int = 0, // Starts at 0
    val priority: String = "Normal" // "High", "Medium", "Normal"
)

/**
 * 30-Day Plan with 4 phases and smart subject rotation (2 subjects / day).
 */
@Entity(tableName = "day_plans")
data class DayPlanEntity(
    @PrimaryKey val dayNumber: Int, // 1 to 30
    val phase: Int, // 1: Foundation (1-10), 2: Practice & Test Books (11-20), 3: Past Papers & Intensive (21-26), 4: Final Mock (27-30)
    val phaseTitle: String,
    val weekNumber: Int, // 1 to 4
    val subject1Id: String,
    val subject2Id: String,
    val goal: String,
    val plannedMinutes: Int = 120, // 2 hours daily target
    val completedMinutes: Int = 0, // Starts at 0
    val isCompleted: Boolean = false,
    val notes: String = ""
)

/**
 * Specific tasks for each day.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayNumber: Int, // 1 to 30
    val subjectId: String,
    val title: String,
    val category: String, // "STUDY", "SHORT_NOTE", "MIND_MAP", "TEST_BOOK", "PAST_PAPER", "REVISION", "MISTAKE_CORRECTION", "MOCK_TEST"
    val plannedMinutes: Int = 25,
    val completedMinutes: Int = 0, // Starts at 0
    val isCompleted: Boolean = false, // Starts at false
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val difficulty: String = "MEDIUM", // "EASY", "MEDIUM", "HARD"
    val isWeakTopic: Boolean = false,
    val notes: String = ""
)

/**
 * The 10 Full-Test Books Tracker.
 */
@Entity(tableName = "test_books")
data class TestBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookNumber: Int, // 1 to 10
    val title: String,
    val subjectId: String,
    val totalQuestions: Int = 100,
    val completedQuestions: Int = 0, // Starts at 0
    val scorePercentage: Int = 0, // Starts at 0
    val mistakesCount: Int = 0, // Starts at 0
    val mistakesNotes: String = "",
    val revisionStatus: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "NEEDS_REVISION", "COMPLETED"
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Past Paper Record.
 */
@Entity(tableName = "past_papers")
data class PastPaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val year: String,
    val term: String, // "1st Term", "2nd Term", "3rd Term / Final", "Model Paper"
    val score: Int = 0, // Starts at 0
    val maxScore: Int = 100,
    val timeTakenMinutes: Int = 0, // Starts at 0
    val mistakes: String = "",
    val isCompleted: Boolean = false, // Starts at false
    val completedDate: Long = 0L
)

/**
 * Short Notes Entity.
 */
@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val topic: String,
    val summary: String,
    val importantFacts: String = "",
    val formulas: String = "",
    val keywords: String = "",
    val questionsToRemember: String = "",
    val isWeakTopic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Mind Map Entity.
 */
@Entity(tableName = "mind_maps")
data class MindMapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val title: String,
    val mainIdea: String,
    val branchesText: String, // Newline or comma separated branches
    val subtopics: String = "",
    val importantKeywords: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Weak Areas, Mistakes & Difficult Topics for targeted revision.
 */
@Entity(tableName = "weak_areas")
data class WeakAreaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val topic: String,
    val type: String, // "WEAK_TOPIC", "TO_REVISE", "MISTAKE_TO_FIX", "HIGH_PRIORITY"
    val severity: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val notes: String = "",
    val isResolved: Boolean = false, // Starts at false
    val addedDate: Long = System.currentTimeMillis()
)

/**
 * Customizable 2-Hour Daily Study Structure Blocks (e.g. 25m/5m/25m/5m/25m/5m/25m).
 */
@Entity(tableName = "study_blocks")
data class StudyBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderIndex: Int,
    val title: String,
    val durationMinutes: Int,
    val isBreak: Boolean = false
)

/**
 * User Streak, Exam Date and Global Settings.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val streakCount: Int = 0, // Starts at 0
    val lastStudyDateMillis: Long = 0L,
    val examDateMillis: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days from now
    val totalAvailableHours: Int = 60,
    val dailyGoalMinutes: Int = 120,
    val isDarkTheme: Boolean = true
)

/**
 * Gamification Achievement Badges.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false, // Starts at false
    val unlockedDate: Long = 0L
)
