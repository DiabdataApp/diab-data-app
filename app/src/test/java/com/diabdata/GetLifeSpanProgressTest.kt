package com.diabdata

import com.diabdata.widget.workers.getLifeSpanProgress
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLifeSpanProgressTest {
    @Test
    fun getLifeSpanProgress_progressIsZero() {
        val today = LocalDate.now()
        val end = today.plusDays(10)

        assertEquals(0, getLifeSpanProgress(today, end))
    }

    @Test
    fun getLifeSpanProgress_progressIsFifty() {
        val end = LocalDate.now().plusDays(5)
        val start = end.minusDays(10)

        assertEquals(50, getLifeSpanProgress(start, end))
    }

    @Test
    fun getLifeSpanProgress_progressIsOneHundred() {
        val end = LocalDate.now()
        val start = end.minusDays(10)

        assertEquals(100, getLifeSpanProgress(start, end))
    }

    @Test(expected = IllegalArgumentException::class)
    fun getLifeSpanProgress_startDateBeforeEndDate() {
        val today = LocalDate.now()
        val start = today.plusDays(1)
        val end = today.minusDays(1)

        getLifeSpanProgress(start, end)
    }
}
