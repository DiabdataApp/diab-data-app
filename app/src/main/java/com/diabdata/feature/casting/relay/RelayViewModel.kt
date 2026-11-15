package com.diabdata.feature.casting.relay

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.UserDetails
import com.diabdata.core.utils.data.GsonFactory
import com.diabdata.feature.casting.castServer.utils.computeTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class RelayViewModel @Inject constructor(
    private val db: DiabDataDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val relayClient = RelayClient()

    val gson = GsonFactory.create()

    // ═══════════════════════════════════════════
    //  STATE
    // ═══════════════════════════════════════════

    val connectionState: StateFlow<ConnectionState> = relayClient.connectionState

    val token: String?
        get() = relayClient.currentToken

    val mode: ShareMode?
        get() = relayClient.currentMode

    // ═══════════════════════════════════════════
    //  ACTIONS
    // ═══════════════════════════════════════════

    fun startSharing(mode: ShareMode, durationMinutes: Int = 30) {
        viewModelScope.launch {
            relayClient.startSharing(mode, durationMinutes)
        }
    }

    fun stopSharing() {
        viewModelScope.launch {
            relayClient.stopSharing()
        }
    }

    // ═══════════════════════════════════════════
    //  INCOMING REQUESTS
    // ═══════════════════════════════════════════

    init {
        viewModelScope.launch {
            relayClient.incomingRequests.collect { forward ->
                handleRequest(forward)
            }
        }
    }

    private suspend fun handleRequest(forward: ForwardMessage) {
        Log.d("RelayVM", "Incoming payload: ${forward.payload}")

        val today = LocalDateTime.now()!!
        val request = gson.fromJson(forward.payload, Map::class.java)
        val route = request["route"] as String
        val method = request["method"] as? String ?: "GET"
        val body = request["body"] as? String

        val responsePayload = when (method) {
            "GET" -> {
                when (route) {
                    "/api/dashboard" -> {
                        val dates = db.importantDateDao().getAllImportantDates().first()
                        val allWeights =
                            db.weightDao().getWeightsSince(today.toLocalDate().minusYears(1))
                                .first()
                        val latestWeight = allWeights.maxByOrNull { it.date }?.let { latest ->
                            mapOf(
                                "value" to latest.value,
                                "date" to latest.date.toString(),
                                "trend" to computeTrend(
                                    entries = allWeights,
                                    valueExtractor = { it.value },
                                    dateExtractor = { it.date }
                                )
                            )
                        }

                        val allHba1c =
                            db.hba1cDao().getHBA1CEntriesSince(today.toLocalDate().minusYears(1))
                                .first()
                        val latestHba1c = allHba1c.maxByOrNull { it.date }?.let { latest ->
                            mapOf(
                                "value" to latest.value,
                                "date" to latest.date.toString(),
                                "trend" to computeTrend(
                                    entries = allHba1c,
                                    valueExtractor = { it.value },
                                    dateExtractor = { it.date }
                                )
                            )
                        }

                        val appointments =
                            db.appointmentDao().getUpcomingAppointmentsFlow(today).first()
                        val treatments =
                            db.treatmentDao().getUpcomingExpirationDatesFlow(today.toLocalDate())
                                .first()

                        mapOf(
                            "importantDates" to dates,
                            "latestWeight" to latestWeight,
                            "latestHba1c" to latestHba1c,
                            "upcomingAppointments" to appointments,
                            "activeTreatments" to treatments
                        )
                    }

                    "/api/user" -> {
                        val userInfo = db.userDetailsDao().getUserDetails().first()
                        userInfo ?: mapOf("message" to "No user profile found")
                    }

                    "/api/user/photo" -> {
                        val userInfo = db.userDetailsDao().getUserDetails().first()

                        val path = userInfo?.profilePhotoPath

                        if (path == null) {
                            mapOf("message" to "No photo path")
                        } else if (!File(path).exists()) {
                            mapOf("message" to "Photo file not found")
                        } else {
                            val file = File(path)

                            val bytes = file.readBytes()
                            val base64 =
                                Base64.encodeToString(
                                    bytes,
                                    Base64.NO_WRAP
                                )
                            val extension = file.extension.lowercase()
                            val mimeType = when (extension) {
                                "png" -> "image/png"
                                "webp" -> "image/webp"
                                "svg" -> "image/svg+xml"
                                else -> "image/jpeg"
                            }
                            mapOf("photo" to "data:$mimeType;base64,$base64")
                        }
                    }

                    "/api/latestMeasures" -> {
                        val weights = db.weightDao().getWeightPlotData(
                            today.minusYears(1).toLocalDate(),
                            today.toLocalDate()
                        ).first()
                        val hba1cs = db.hba1cDao().getHBA1CPlotData(
                            today.minusYears(1).toLocalDate(),
                            today.toLocalDate()
                        ).first()
                        mapOf("weights" to weights, "hba1cs" to hba1cs)
                    }

                    else -> {
                        mapOf("error" to "Unknown route")
                    }
                }
            }
            "POST" -> {
                mapOf("error" to "Not implemented")
            }
            "PUT" -> {
                when (route) {
                    "/api/updateUser" -> {
                        Log.d("RelayVM", "Payload reçu: ${forward.payload}")
                        val updatedUser = gson.fromJson(body, UserDetails::class.java)
                        try {
                            db.userDetailsDao().upsertUserDetails(updatedUser)
                            mapOf("success" to "User profile updated")
                        } catch (e: Exception) {
                            mapOf("error" to "Failed to update user profile")
                        }
                    }
                    else -> {
                        mapOf("error" to "Unknown route")
                    }
                }
            }
            "DELETE" -> {
                mapOf("error" to "Not implemented")
            }
            else -> {
                mapOf("error" to "Unknown method")
            }
        }

        relayClient.sendResponse(
            requestId = forward.requestId,
            clientId = forward.clientId,
            payload = gson.toJson(responsePayload)
        )
    }
}
