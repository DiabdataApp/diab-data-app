package com.diabdata.feature.dataMatrixScanner.utils

import android.content.Context
import androidx.core.content.edit
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.MedicalDeviceInfoEntity
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import java.io.InputStream
import java.security.MessageDigest

class MedicalDevicesInitializer(private val context: Context, private val db: DiabDataDatabase) {

    suspend fun initialize() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val currentHash = calculateHash(context.assets.open("medical_devices.csv"))
        val savedHash = prefs.getString("medical_devices_csv_hash", null)

        val isEmpty = db.medicalDevicesInfoDao().countAll() == 0

        if (isEmpty || currentHash != savedHash) {
            val devices = loadCsvFromAssets()
            db.medicalDevicesInfoDao().insertAll(devices) // insertion en masse
            prefs.edit { putString("medical_devices_csv_hash", currentHash) }
        }
    }

    private fun loadCsvFromAssets(): List<MedicalDeviceInfoEntity> {
        val inputStream = context.assets.open("medical_devices.csv")
        val reader = inputStream.bufferedReader()
        val result = mutableListOf<MedicalDeviceInfoEntity>()

        reader.readLines().drop(1)
            .forEach { line ->
                val cols = line.split(",")
                if (cols.size >= 4) {
                    result.add(
                        MedicalDeviceInfoEntity(
                            cipGtin = cols[0].trim(),
                            manufacturer = cols[1].trim(),
                            deviceType = MedicalDeviceInfoType.valueOf(cols[2].trim()),
                            fullName = cols[3].trim(),
                            daysLifespan = cols[4].trim().toInt()
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
