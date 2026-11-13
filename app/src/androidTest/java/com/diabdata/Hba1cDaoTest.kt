package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.hba1c.data.HBA1CDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

/**
 * Tests for [HBA1CDao] found at [com.diabdata.feature.hba1c.data.HBA1CDao].
 */
@RunWith(AndroidJUnit4::class)
class Hba1cDaoTest {
    private lateinit var hba1cDao: HBA1CDao
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
        hba1cDao = db.hba1cDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHba1c() = runBlocking {
        val hba1c: Hba1c = Hba1c(
            date = today,
            createdAt = now,
            isArchived = false,
            value = 7.0f,
            updatedAt = now,
            entryUuid = "hba1c-1"
        )

        hba1cDao.insert(hba1c)
        val allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(1, allHba1c.size)
        assertEquals(hba1c.value, allHba1c[0].value, 0.001f)
        assertEquals(hba1c.date, allHba1c[0].date)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndUpdateHba1c() = runBlocking {
        val hba1c: Hba1c = Hba1c(
            id=1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 7.0f,
            updatedAt = now,
            entryUuid = "hba1c-2"
        )

        hba1cDao.insert(hba1c)
        hba1cDao.update(hba1c.copy(value = 6.9f))

        val allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(1, allHba1c.size)
        assertEquals(6.9f, allHba1c[0].value, 0.001f)
        assertEquals(hba1c.date, allHba1c[0].date)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDeleteHba1c() = runBlocking {
        val hba1c: Hba1c = Hba1c(
            id = 1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 7.0f,
            updatedAt = now,
            entryUuid = "hba1c-3"
        )

        hba1cDao.insert(hba1c)
        hba1cDao.deleteById(hba1c.id)

        val allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(0, allHba1c.size)
    }

    @Test
    @Throws(Exception::class)
    fun insertMultipleAndCheckIfFirstIsMostRecent() = runBlocking {
        val first = today.minusDays(3)
        val second = today.minusDays(8)
        val third = today.minusDays(16)

        val hba1cs: List<Hba1c> = listOf(
            Hba1c(
                date = third,
                createdAt = now,
                isArchived = false,
                value = 6.8f,
                updatedAt = now,
                entryUuid = "hba1c-4"
            ),
            Hba1c(
                date = first,
                createdAt = now,
                isArchived = false,
                value = 7.1f,
                updatedAt = now,
                entryUuid = "hba1c-5"
            ),
            Hba1c(
                date = second,
                createdAt = now,
                isArchived = false,
                value = 6.7f,
                updatedAt = now,
                entryUuid = "hba1c-6"
            )
        )

        hba1cs.forEach { hba1cDao.insert(it) }

        val allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(3, allHba1c.size)
        assertEquals(first, allHba1c[0].date)
    }

    @Test
    @Throws(Exception::class)
    fun setArchived() = runBlocking {
        val hba1c: Hba1c = Hba1c(
            id = 1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 7.0f,
            updatedAt = now,
            entryUuid = "hba1c-7"
        )

        hba1cDao.insert(hba1c)

        var allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(1, allHba1c.size)
        assertEquals(false, allHba1c[0].isArchived)

        hba1cDao.update(hba1c.copy(isArchived = true))

        allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(1, allHba1c.size)
        assertEquals(true, allHba1c[0].isArchived)

        hba1cDao.update(hba1c.copy(isArchived = false))

        allHba1c = hba1cDao.getAllHBA1CFlow().first()

        assertEquals(1, allHba1c.size)
        assertEquals(false, allHba1c[0].isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun getHba1csSince() = runBlocking {
        val endDate = today.minusMonths(3)

        val first = today.minusDays(3)
        val second = today.minusDays(8).minusMonths(2)
        val third = today.minusDays(16).minusMonths(8)
        val fourth = today.minusDays(22).minusMonths(1)


        val hba1cs: List<Hba1c> = listOf(
            Hba1c(
                date = third,
                createdAt = now,
                isArchived = false,
                value = 7.3f,
                updatedAt = now,
                entryUuid = "hba1c-8"
            ),
            Hba1c(
                date = first,
                createdAt = now,
                isArchived = false,
                value = 7.0f,
                updatedAt = now,
                entryUuid = "hba1c-9"
            ),
            Hba1c(
                date = second,
                createdAt = now,
                isArchived = false,
                value = 6.8f,
                updatedAt = now,
                entryUuid = "hba1c-10"
            ),
            Hba1c(
                date = fourth,
                createdAt = now,
                isArchived = true,
                value = 7.4f,
                updatedAt = now,
                entryUuid = "hba1c-11"
            )
        )

        hba1cs.forEach { hba1cDao.insert(it) }

        val allHba1c = hba1cDao.getHBA1CEntriesSince(endDate).first()

        assertEquals(2, allHba1c.size)
    }

    @Test
    @Throws(Exception::class)
    fun getHba1cPlotData() = runBlocking {
        val startDate = today.minusMonths(3)

        val first = today.minusDays(3)
        val second = today.minusDays(8).minusMonths(2)
        val third = today.minusDays(16).minusMonths(8)
        val fourth = today.minusDays(22).minusMonths(1)


        val hba1cs: List<Hba1c> = listOf(
            Hba1c(
                date = third,
                createdAt = now,
                isArchived = false,
                value = 7.3f,
                updatedAt = now,
                entryUuid = "hba1c-12"
            ),
            Hba1c(
                date = first,
                createdAt = now,
                isArchived = false,
                value = 7.0f,
                updatedAt = now,
                entryUuid = "hba1c-13"
            ),
            Hba1c(
                date = second,
                createdAt = now,
                isArchived = false,
                value = 6.8f,
                updatedAt = now,
                entryUuid = "hba1c-14"
            ),
            Hba1c(
                date = fourth,
                createdAt = now,
                isArchived = true,
                value = 7.4f,
                updatedAt = now,
                entryUuid = "hba1c-15"
            )
        )

        hba1cs.forEach { hba1cDao.insert(it) }

        val hba1cPoints = hba1cDao.getHBA1CPlotData(startDate, today).first()

        assertEquals(2, hba1cPoints.size)
        assertEquals(second, hba1cPoints[0].date)
    }
}