package com.diabdata

import com.diabdata.widget.workers.getDaysLeft
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDaysLeftTest {
    @Test
    fun getDaysLeft_isTen() {
        val today = LocalDate.now()
        val endDate = today.plusDays(10)

        assertEquals(10, getDaysLeft(endDate))
    }

    @Test
    fun getDaysLeft_isZero() {
        val today = LocalDate.now()

        assertEquals(0, getDaysLeft(today))
    }

    @Test
    fun getDaysLeft_inThePastIsNegative() {
        val today = LocalDate.now()
        val endDate = today.minusDays(10)

        assertEquals(-10, getDaysLeft(endDate))
    }

    @Test
    fun getDaysLeft_isOne() {
        val today = LocalDate.now()
        val endDate = today.plusDays(1)

        assertEquals(1, getDaysLeft(endDate))
    }

    @Test
    fun getDaysLeft_isMinusOne() {
        val today = LocalDate.now()
        val endDate = today.minusDays(1)

        assertEquals(-1, getDaysLeft(endDate))
    }

    @Test
    fun getDaysLeft_worksInTheFarFuture() {
        val today = LocalDate.now()
        val end = today.plusDays(365)

        assertEquals(365, getDaysLeft(end))
    }
}
