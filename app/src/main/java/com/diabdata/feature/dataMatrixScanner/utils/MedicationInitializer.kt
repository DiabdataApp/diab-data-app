package com.diabdata.feature.dataMatrixScanner.utils

import android.content.Context
import androidx.core.content.edit
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Medication
import com.diabdata.shared.utils.dataTypes.TreatmentType
import java.io.InputStream
import java.security.MessageDigest

class MedicationInitializer(private val context: Context, private val db: DiabDataDatabase) {

    suspend fun initialize() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val currentHash = calculateHash(context.assets.open("medication_data.csv"))
        val savedHash = prefs.getString("csv_hash", null)

        val isEmpty = db.medicationDao().countAll() == 0

        if (isEmpty || currentHash != savedHash) {
            val medications = loadCsvFromAssets()
            db.medicationDao().insertAll(medications) // insertion en masse
            prefs.edit { putString("csv_hash", currentHash) }
        }
    }

    private fun loadCsvFromAssets(): List<Medication> {
        val inputStream = context.assets.open("medication_data.csv")
        val reader = inputStream.bufferedReader()
        val result = mutableListOf<Medication>()

        reader.readLines().drop(1)
            .forEach { line ->
                val cols = line.split(",")
                if (cols.size >= 4) {
                    result.add(
                        Medication(
                            cipGtin = cols[0].trim(),
                            brandName = cols[1].trim(),
                            treatmentType = TreatmentType.valueOf(cols[2].trim()),
                            fullName = cols[3].trim()
                        )
                    )
                }
            }

        return result
    }

    private fun calculateHash(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var read: Int

        while (input.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
        input.close()

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
