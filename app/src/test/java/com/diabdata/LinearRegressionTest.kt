package com.diabdata

import com.diabdata.core.utils.linearRegression
import org.junit.Assert.assertEquals
import org.junit.Test

class LinearRegressionTest {
    // Linear regression tests
    @Test
    fun linearRegression_isPositiveSlope() {
        val x = listOf(1.0, 2.0, 3.0)
        val y = listOf(1.0, 2.0, 3.0)

        val fn = linearRegression(x, y)

        assertEquals(1.0, fn(1.0), 0.001)
        assertEquals(2.0, fn(2.0), 0.001)
        assertEquals(3.0, fn(3.0), 0.001)
    }

    @Test
    fun linearRegression_isNegativeSlope() {
        val x = listOf(1.0, 2.0, 3.0)
        val y = listOf(3.0, 2.0, 1.0)

        val fn = linearRegression(x, y)

        assertEquals(3.0, fn(1.0), 0.001)
        assertEquals(2.0, fn(2.0), 0.001)
        assertEquals(1.0, fn(3.0), 0.001)
    }

    @Test
    fun linearRegression_isHorizontal() {
        val x = listOf(1.0, 2.0, 3.0)
        val y = listOf(5.0, 5.0, 5.0)

        val fn = linearRegression(x, y)

        assertEquals(5.0, fn(1.0), 0.001)
        assertEquals(5.0, fn(2.0), 0.001)
        assertEquals(5.0, fn(3.0), 0.001)
    }

    @Test
    fun linearRegression_lineGoesThroughTwoPoints() {
        val x = listOf(1.0, 2.0)
        val y = listOf(1.0, 2.0)

        val fn = linearRegression(x, y)

        assertEquals(1.0, fn(1.0), 0.001)
        assertEquals(2.0, fn(2.0), 0.001)
    }

    @Test
    fun linearRegression_linePredictionContinuesOutsidePointSet() {
        val x = listOf(1.0, 2.0, 3.0)
        val y = listOf(1.0, 2.0, 3.0)

        val fn = linearRegression(x, y)

        assertEquals(10.0, fn(10.0), 0.001)
        assertEquals(-10.0, fn(-10.0), 0.001)
    }

    // Linear regression error test
    @Test(expected = IllegalArgumentException::class)
    fun linearRegression_errorOnEmptyXSet() {
        val x = listOf<Double>()
        val y = listOf(1.0, 2.0, 3.0)

        linearRegression(x, y)
    }

    @Test(expected = IllegalArgumentException::class)
    fun linearRegression_errorOnEmptyYSet() {
        val x = listOf(1.0, 2.0, 3.0)
        val y = listOf<Double>()

        linearRegression(x, y)
    }

    @Test(expected = IllegalArgumentException::class)
    fun linearRegression_errorOnMismatchedPointSetSizes() {
        val x = listOf(1.0, 2.0, 3.0)
        val y = listOf(1.0, 2.0)

        linearRegression(x, y)
    }
}
