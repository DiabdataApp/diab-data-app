package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Treatment
import com.diabdata.core.model.nowMillis
import com.diabdata.core.model.nowMillisInLong
import com.diabdata.feature.treatments.data.TreatmentDao
import com.diabdata.shared.utils.dataTypes.TreatmentType
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
 * Tests for [TreatmentDao] found at [com.diabdata.feature.treatments.data.TreatmentDao].
 */
@RunWith(AndroidJUnit4::class)
class TreatmentDaoTest {
    private lateinit var db: DiabDataDatabase
    private lateinit var treatmentDao: TreatmentDao

    val today: LocalDate = LocalDate.now()
    val now = nowMillis()

    val treatment = Treatment(
        id = 1,
        expirationDate = today,
        name = "fast acting insulin syringe",
        createdAt = now,
        isArchived = false,
        type = TreatmentType.FAST_ACTING_INSULIN_SYRINGE,
        updatedAt = now,
        entryUuid = "treatment-1"
    )
    val treatment2 = treatment.copy(
        id = 2,
        expirationDate = today.plusDays(8),
        entryUuid = "treatment-2"
    )
    val treatment3 = treatment.copy(
        id = 3,
        expirationDate = today.plusDays(1),
        entryUuid = "treatment-3"
    )
    val treatment4 = treatment.copy(
        id = 4,
        expirationDate = today.minusDays(4),
        entryUuid = "treatment-4"
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        treatmentDao = db.treatmentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertTreatment() = runBlocking {
        treatmentDao.insert(treatment)

        val treatments = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(treatment, treatments[0])
    }

    @Test
    @Throws(Exception::class)
    fun updateTreatment() = runBlocking {
        treatmentDao.insert(treatment)

        val treatmentsBefore = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(treatment, treatmentsBefore[0])

        val updatedTreatment = treatment.copy(name = "updated treatment")
        treatmentDao.update(updatedTreatment)

        val treatmentsAfter = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(updatedTreatment, treatmentsAfter[0])
    }

    @Test
    @Throws(Exception::class)
    fun deleteTreatmentById() = runBlocking {
        treatmentDao.insert(treatment)

        val treatmentsBefore = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(treatment, treatmentsBefore[0])

        treatmentDao.deleteById(treatment.id)
        val treatmentsAfter = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(0, treatmentsAfter.size)
    }

    @Test
    @Throws(Exception::class)
    fun insertTreatmentAndSetArchived() = runBlocking {
        treatmentDao.insert(treatment)

        val treatmentsBefore = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(treatment, treatmentsBefore[0])

        treatmentDao.setArchived(treatment.id, true, nowMillisInLong())
        val treatmentsAfterArchive = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(true, treatmentsAfterArchive[0].isArchived)

        treatmentDao.setArchived(treatment.id, false, nowMillisInLong())
        val treatmentsAfterUnarchive = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(false, treatmentsAfterUnarchive[0].isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun insertMultipleAndGetAll() = runBlocking {
        val treatmentsToInsert = listOf(treatment4, treatment, treatment3, treatment2)
        treatmentsToInsert.forEach {
            treatmentDao.insert(it)
        }

        val treatments = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(4, treatments.size)
        assertEquals(treatment2, treatments[0])
    }

    @Test
    @Throws(Exception::class)
    fun insertMultipleAndGetAllSortedByExpirationDate() = runBlocking {
        val treatmentsToInsert = listOf(treatment4, treatment, treatment3, treatment2)
        treatmentsToInsert.forEach {
            treatmentDao.insert(it)
        }

        val allTreatments = treatmentDao.getAllTreatmentsFlow().first()
        assertEquals(4, allTreatments.size)

        val upcomingTreatments = treatmentDao.getUpcomingExpirationDatesFlow(today).first()
        assertEquals(3, upcomingTreatments.size)
        assertEquals(treatment, upcomingTreatments[0])
    }
}