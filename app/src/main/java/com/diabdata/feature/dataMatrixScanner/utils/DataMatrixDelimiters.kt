package com.diabdata.feature.dataMatrixScanner.utils

fun generateDataMatrixDelimiters(excludedDelimiters: List<String> = emptyList()): String {
    val delimiters = listOf(
        "01", // GTIN/CIP
        "10", // BATCH NUMBER
        "11", // FABRICATION DATE
        "17", // EXPIRATION DATE
        "21", // SERIAL NUMBER
        "\u001D", // DATA MATRIX DELIMITER CHARACTER
        "$"
    )

    val filtered = delimiters.filterNot { it in excludedDelimiters }

    return "(?:${filtered.joinToString("|")})"
}
