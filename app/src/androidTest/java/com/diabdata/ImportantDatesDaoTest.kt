package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.importantDates.data.ImportantDateDao
import org.junit.Assert.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

/**
 * Tests for [ImportantDateDao] found at [com.diabdata.feature.importantDates.data.ImportantDateDao]
 */
@RunWith(AndroidJUnit4::class)
class ImportantDatesDaoTest {
    private lateinit var importantDateDao: ImportantDateDao
    private lateinit var db: DiabDataDatabase

    val today: LocalDate = LocalDate.now()
    val now = nowMillis()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        importantDateDao = db.importantDateDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetImportantDate() = runBlocking {
        val importantDate = ImportantDate(
            date = today,
            createdAt = now,
            isArchived = false,
            importantDate = "test",
            id = 1,
            updatedAt = now,
            entryUuid = "important-date-1"
        )

        importantDateDao.insert(importantDate)
        val allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndUpdateImportantDate() = runBlocking {
        val importantDate = ImportantDate(
            date = today,
            createdAt = now,
            isArchived = false,
            importantDate = "test",
            id = 1,
            updatedAt = now,
            entryUuid = "important-date-2"
        )

        importantDateDao.insert(importantDate)
        var allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
        assertEquals(today, allImportantDates[0].date)

        importantDateDao.update(allImportantDates[0].copy(importantDate = "test2"))
        allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
        assertEquals("test2", allImportantDates[0].importantDate)
    }

    @Test
    @Throws(Exception::class)
    fun setArchivedImportantDate() = runBlocking {
        val importantDate = ImportantDate(
            date = today,
            createdAt = now,
            isArchived = false,
            importantDate = "test",
            id = 1,
            updatedAt = now,
            entryUuid = "important-date-3"
        )

        importantDateDao.insert(importantDate)
        var allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
        assertEquals(false, allImportantDates[0].isArchived)

        importantDateDao.update(importantDate.copy(isArchived = true))
        allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
        assertEquals(true, allImportantDates[0].isArchived)

        importantDateDao.update(importantDate.copy(isArchived = false))
        allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
        assertEquals(false, allImportantDates[0].isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun deleteImportantDate() = runBlocking {
        val importantDate = ImportantDate(
            date = today,
            createdAt = now,
            isArchived = false,
            importantDate = "test",
            id = 1,
            updatedAt = now,
            entryUuid = "important-date-4"
        )

        importantDateDao.insert(importantDate)
        var allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(1, allImportantDates.size)
        importantDateDao.deleteById(importantDate.id)

        allImportantDates = importantDateDao.getAllImportantDates().first()
        assertEquals(0, allImportantDates.size)
    }

    @Test
    @Throws(Exception::class)
    fun getALlImportantDates() = runBlocking {
        val importantDates = listOf(
            ImportantDate(
                date = today,
                createdAt = now,
                isArchived = false,
                importantDate = "Test 1",
                id = 1,
                updatedAt = now,
                entryUuid = "important-date-5"
            ),
            ImportantDate(
                date = today.minusYears(1).minusMonths(2),
                createdAt = now,
                isArchived = false,
                importantDate = "Test 2",
                id = 2,
                updatedAt = now,
                entryUuid = "important-date-6"
            ),
        )

        importantDates.forEach { importantDateDao.insert(it) }
        val allImportantDates = importantDateDao.getAllImportantDates().first()

        assertEquals(2, allImportantDates.size)
        assertEquals("Test 1", allImportantDates[0].importantDate)
    }
}