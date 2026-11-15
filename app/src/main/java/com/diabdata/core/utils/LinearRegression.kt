package com.diabdata.core.utils

fun linearRegression(x: List<Double>, y: List<Double>): (Double) -> Double {
    require(x.size == y.size) { "x and y must have the same size" }
    require(x.isNotEmpty()) { "x must not be empty" }
    require(y.isNotEmpty()) { "y must not be empty" }

    val n = x.size
    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y).sumOf { (xi, yi) -> xi * yi }
    val sumX2 = x.sumOf { xi -> xi * xi }

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - slope * sumX) / n

    return { xi -> slope * xi + intercept }
}
