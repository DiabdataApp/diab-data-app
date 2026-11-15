package com.diabdata.feature.dataMatrixScanner.utils

data class MedicalDeviceInfo(
    val gtin: String,
    val lot: String,
    val expiration: String?,
    val serialNumber: String?,
    val referenceNumber: String?
)

fun parseMedicalDevice(raw: String): MedicalDeviceInfo {
    // Log.d("MedicalDevice", "Parsing medical devices: $raw")

    val cleaned = raw.trim().replace("\u0004", "").replace("\u001E", "")
    val parts = cleaned.split('\u001D')

    var gtin: String? = null
    var lot: String? = null
    var expiration: String? = null
    var serial: String? = null
    var reference: String? = null

    fun parsePart(part: String) {
        val subRegex =
            Regex(
                "(01\\d{14})|(10[^\\u001D]*)|(11\\d{6})|(17\\d{6,8})|(21[^\\u001D]*)|(241[^\\u001D]*)"
            )
        subRegex.findAll(part).forEach { match ->
            val data = match.value
            when {
                data.startsWith("01") -> gtin = data.removePrefix("01").take(14)
                data.startsWith("10") -> lot = data.removePrefix("10")
                data.startsWith("17") -> expiration = parseDate(data.removePrefix("17"))
                data.startsWith("21") -> serial = data.removePrefix("21")
                data.startsWith("241") -> reference = data.removePrefix("241")
                data.startsWith("11") -> {}
            }
        }
    }

    parts.forEach { parsePart(it) }

    return MedicalDeviceInfo(
        gtin = gtin ?: "",
        lot = lot ?: "XXX",
        expiration = expiration,
        serialNumber = serial,
        referenceNumber = reference
    )
}

private fun parseDate(raw: String): String = when (raw.length) {
    6 -> {
        val yy = raw.substring(0, 2)
        val mm = raw.substring(2, 4)
        val dd = raw.substring(4, 6)
        "20$yy-$mm-$dd"
    }

    8 -> {
        val yyyy = raw.substring(0, 4)
        val mm = raw.substring(4, 6)
        val dd = raw.substring(6, 8)
        "$yyyy-$mm-$dd"
    }

    else -> raw
}
