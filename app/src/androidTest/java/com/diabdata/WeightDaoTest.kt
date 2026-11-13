package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Weight
import com.diabdata.core.model.nowMillis
import com.diabdata.feature.weight.data.WeightDao
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
 * Tests for [WeightDao] found at [com.diabdata.feature.weight.data.WeightDao].
 */
@RunWith(AndroidJUnit4::class)
class WeightDaoTest {
    private lateinit var weightDao: WeightDao
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
        weightDao = db.weightDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetWeight() = runBlocking {
        val weight: Weight = Weight(
            id = 1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 70f,
            updatedAt = now,
            entryUuid = "weight-1"
        )

        weightDao.insert(weight)
        val allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(1, allWeights.size)
        assertEquals(weight.value, allWeights[0].value, 0.001f)
        assertEquals(weight.date, allWeights[0].date)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndUpdateWeight() = runBlocking {
        val weight: Weight = Weight(
            id = 1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 70f,
            updatedAt = now,
            entryUuid = "weight-2"
        )

        weightDao.insert(weight)
        weightDao.update(weight.copy(value = 79f))

        val allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(1, allWeights.size)
        assertEquals(79f, allWeights[0].value, 0.001f)
        assertEquals(weight.date, allWeights[0].date)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndDeleteWeight() = runBlocking {
        val weight: Weight = Weight(
            id = 1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 70f,
            updatedAt = now,
            entryUuid = "weight-3"
        )

        weightDao.insert(weight)
        weightDao.deleteById(weight.id)

        val allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(0, allWeights.size)
    }

    @Test
    @Throws(Exception::class)
    fun insertMultipleAndCheckIfFirstIsMostRecent() = runBlocking {
        val first = today.minusDays(3)
        val second = today.minusDays(8)
        val third = today.minusDays(16)

        val weights: List<Weight> = listOf(
            Weight(
                date = third,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-4"
            ),
            Weight(
                date = first,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-5"
            ),
            Weight(
                date = second,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-6"
            )
        )

        weights.forEach { weightDao.insert(it) }

        val allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(3, allWeights.size)
        assertEquals(first, allWeights[0].date)
    }

    @Test
    @Throws(Exception::class)
    fun setArchived() = runBlocking {
        val weight: Weight = Weight(
            id = 1,
            date = today,
            createdAt = now,
            isArchived = false,
            value = 70f,
            updatedAt = now,
            entryUuid = "weight-7"
        )

        weightDao.insert(weight)

        var allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(1, allWeights.size)
        assertEquals(false, allWeights[0].isArchived)

        weightDao.update(weight.copy(isArchived = true))

        allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(1, allWeights.size)
        assertEquals(true, allWeights[0].isArchived)

        weightDao.update(weight.copy(isArchived = false))

        allWeights = weightDao.getAllWeightsFlow().first()

        assertEquals(1, allWeights.size)
        assertEquals(false, allWeights[0].isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun getWeightsSince() = runBlocking {
        val endDate = today.minusMonths(3)
        
        val first = today.minusDays(3)
        val second = today.minusDays(8).minusMonths(2)
        val third = today.minusDays(16).minusMonths(8)
        val fourth = today.minusDays(22).minusMonths(1)


        val weights: List<Weight> = listOf(
            Weight(
                date = third,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-8"
            ),
            Weight(
                date = first,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-9"
            ),
            Weight(
                date = second,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-10"
            ),
            Weight(
                date = fourth,
                createdAt = now,
                isArchived = true,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-11"
            )
        )

        weights.forEach { weightDao.insert(it) }

        val allWeights = weightDao.getWeightsSince(endDate).first()

        assertEquals(2, allWeights.size)
    }

    @Test
    @Throws(Exception::class)
    fun getWeightPlotData() = runBlocking {
        val startDate = today.minusMonths(3)

        val first = today.minusDays(3)
        val second = today.minusDays(8).minusMonths(2)
        val third = today.minusDays(16).minusMonths(8)
        val fourth = today.minusDays(22).minusMonths(1)


        val weights: List<Weight> = listOf(
            Weight(
                date = third,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-12"
            ),
            Weight(
                date = first,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-13"
            ),
            Weight(
                date = second,
                createdAt = now,
                isArchived = false,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-14"
            ),
            Weight(
                date = fourth,
                createdAt = now,
                isArchived = true,
                value = 70f,
                updatedAt = now,
                entryUuid = "weight-15"
            )
        )

        weights.forEach { weightDao.insert(it) }

        val weightsPoints = weightDao.getWeightPlotData(startDate, today).first()

        assertEquals(2, weightsPoints.size)
        assertEquals(second, weightsPoints[0].date)
    }
}