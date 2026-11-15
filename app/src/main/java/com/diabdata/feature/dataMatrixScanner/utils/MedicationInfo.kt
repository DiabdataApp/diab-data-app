package com.diabdata.feature.dataMatrixScanner.utils

data class MedicationInfo(
    val gtin: String,
    val lot: String?,
    val expiration: String?,
    val serial: String?
)

fun parseMedication(raw: String): MedicationInfo {
    val cleaned = raw.trim()

    val regex = Regex(
        "01(\\d{14})|" + // GTIN
            "17(\\d{6})|" + // Expiration
            "10(.*?)" + generateDataMatrixDelimiters(listOf("10")) + // Batch
            "21(.*?)" + generateDataMatrixDelimiters(listOf("21")) // Serial
    )

    var gtin: String? = null
    var exp: String? = null
    var lot: String? = null
    var serial: String? = null

    regex.findAll(cleaned).forEach { match ->
        when {
            match.groups[1] != null -> gtin = match.groups[1]?.value
            match.groups[2] != null -> {
                val rawDate = match.groups[2]?.value ?: ""
                exp = when (rawDate.length) {
                    4 -> { // MMYY
                        val mm = rawDate.substring(0, 2)
                        val yy = rawDate.substring(2, 4)
                        "20$yy-$mm-01"
                    }

                    6 -> { // YYMMDD
                        val yy = rawDate.substring(0, 2)
                        val mm = rawDate.substring(2, 4)
                        val dd = rawDate.substring(4, 6)
                        "20$yy-$mm-$dd"
                    }

                    8 -> { // YYYYMMDD
                        val yyyy = rawDate.substring(0, 4)
                        val mm = rawDate.substring(4, 6)
                        val dd = rawDate.substring(6, 8)
                        "$yyyy-$mm-$dd"
                    }

                    else -> rawDate
                }
            }

            match.groups[3] != null -> lot = match.groups[3]?.value
            match.groups[4] != null -> serial = match.groups[4]?.value
        }
    }

    return MedicationInfo(gtin ?: "", lot, exp, serial)
}
