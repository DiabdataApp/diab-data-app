package com.diabdata

import com.diabdata.core.model.Weight
import com.diabdata.core.utils.data.GsonFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class BackupDateFormatTest {
    private val gson = GsonFactory.create()
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun saveAndChangeZoneId() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
    }

    @After
    fun restoreZoneId() {
        TimeZone.setDefault(originalTimeZone)
    }

    private fun readWeight(json: String): Weight {
        return gson.fromJson(json, Weight::class.java)
    }

    private fun writeWeight(weight: Weight): String {
        return gson.toJson(weight)
    }

    @Test
    fun oldBackupDateIsReadAsStartOfDay() {
        val weightJson = """
            {
                "id": 0,
                "date": "2026-01-15",
                "is_archived": false,
                "value": 67.0,
                "created_at": "2026-01-15",
                "updated_at": "2026-01-15"
            }
        """.trimIndent()

        val weight = readWeight(weightJson)

        val createdAtToCompare = LocalDateTime.of(2026, 1, 15, 0, 0)

        val weightCreatedAt = weight.createdAt.toJavaInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        assertEquals(createdAtToCompare, weightCreatedAt)
    }

    @Test
    fun newBackupMillisAreReadAsIs() {
        val weightJson = """
            {
                "id": 0,
                "date": "2026-01-15",
                "is_archived": false,
                "value": 67.0,
                "created_at": 1768435200000,
                "updated_at": 1768435200000
            }
        """.trimIndent()

        val weight = readWeight(weightJson)

        assertEquals(1768435200000L, weight.createdAt.toEpochMilliseconds())
    }

    @Test
    fun instantSurvivesRoundTrip() {
        val weight = Weight(
            id = 1,
            date = LocalDate.of(2026, 12, 12),
            createdAt = Instant.fromEpochMilliseconds(1768435200000),
            isArchived = false,
            value = 67.0f,
            updatedAt = Instant.fromEpochMilliseconds(1768521600000),
            entryUuid = "weight-test"
        )

        val restored = readWeight(writeWeight(weight))

        assertEquals(weight.createdAt, restored.createdAt)
        assertEquals(weight.updatedAt, restored.updatedAt)
    }

    @Test
    fun instantIsWrittenAsMillis() {
        val weight = Weight(
            id = 1,
            date = LocalDate.of(2026, 12, 12),
            createdAt = Instant.fromEpochMilliseconds(1768435200000),
            isArchived = false,
            value = 67.0f,
            updatedAt = Instant.fromEpochMilliseconds(1768521600000),
            entryUuid = "weight-test-2"
        )

        val weightJson = writeWeight(weight)

        assertTrue(weightJson.contains("\"created_at\":1768435200000"))
    }
}