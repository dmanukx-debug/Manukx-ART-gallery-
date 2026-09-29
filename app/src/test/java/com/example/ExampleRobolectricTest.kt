package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TestBookEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: StudyRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StudyRepository(database.studyDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Study30", appName)
    }

    @Test
    fun testInsertAndRetrieveSubject() = runBlocking {
        val math = SubjectEntity(
            id = "math",
            name = "Mathematics",
            category = "Heavy",
            colorHex = 0xFF4F46E5,
            iconName = "calculate",
            targetHours = 8,
            completedMinutes = 0,
            priority = "High"
        )
        database.studyDao().insertSubjects(listOf(math))

        val subjects = repository.allSubjects.first()
        assertEquals(1, subjects.size)
        assertEquals("Mathematics", subjects[0].name)
        assertEquals(8, subjects[0].targetHours)
        assertEquals(0, subjects[0].completedMinutes)
    }

    @Test
    fun testToggleTaskCompletion() = runBlocking {
        val task = TaskEntity(
            dayNumber = 1,
            subjectId = "math",
            title = "Solve algebra questions",
            category = "STUDY",
            plannedMinutes = 25,
            completedMinutes = 0,
            isCompleted = false,
            priority = "HIGH",
            difficulty = "MEDIUM",
            isWeakTopic = true
        )
        val taskId = repository.insertTask(task)
        val inserted = repository.getTasksForDay(1).first().first { it.id == taskId }

        assertFalse(inserted.isCompleted)
        assertEquals(0, inserted.completedMinutes)

        repository.toggleTaskCompleted(inserted)
        val updated = repository.getTasksForDay(1).first().first { it.id == taskId }
        assertTrue(updated.isCompleted)
        assertEquals(25, updated.completedMinutes)
    }

    @Test
    fun testTestBookProgressUpdate() = runBlocking {
        val book = TestBookEntity(
            bookNumber = 1,
            title = "Mathematics Master Test Book",
            subjectId = "math",
            totalQuestions = 100,
            completedQuestions = 0,
            scorePercentage = 0,
            mistakesCount = 0,
            revisionStatus = "NOT_STARTED"
        )
        database.studyDao().insertTestBooks(listOf(book))

        val books = repository.allTestBooks.first()
        assertEquals(1, books.size)
        assertEquals(0, books[0].completedQuestions)

        val updatedBook = books[0].copy(
            completedQuestions = 25,
            scorePercentage = 80,
            revisionStatus = "IN_PROGRESS"
        )
        repository.updateTestBook(updatedBook)

        val afterUpdate = repository.allTestBooks.first()
        assertEquals(25, afterUpdate[0].completedQuestions)
        assertEquals(80, afterUpdate[0].scorePercentage)
        assertEquals("IN_PROGRESS", afterUpdate[0].revisionStatus)
    }
}
