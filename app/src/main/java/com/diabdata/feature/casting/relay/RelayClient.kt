package com.diabdata.feature.casting.relay

import com.diabdata.BuildConfig
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "RelayClient"

class RelayClient {
    companion object {
        /**
         * Relay server host, configured via local.properties or CI secrets.
         */
        private const val RELAY_HOST = BuildConfig.RELAY_SERVER_URL
        private const val RELAY_PORT = 443
        private const val RELAY_PATH = "/ws/app"
        private const val USE_SSL = true
    }

    private val gson = Gson()

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private var session: WebSocketSession? = null
    private var job: Job? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingRequests = MutableSharedFlow<ForwardMessage>()
    val incomingRequests: SharedFlow<ForwardMessage> = _incomingRequests.asSharedFlow()

    var currentToken: String? = null
        private set
    var currentSessionId: String? = null
        private set
    var currentMode: ShareMode? = null
        private set

    suspend fun startSharing(mode: ShareMode, durationMinutes: Int = 30) {
        val token = TokenGenerator.generateToken(mode)
        val tokenHash = TokenGenerator.hashToken(token)
        val sessionId = TokenGenerator.generateSessionId()
        val expiresAt = (System.currentTimeMillis() / 1000) + (durationMinutes * 60)

        currentToken = token
        currentSessionId = sessionId
        currentMode = mode

        _connectionState.value = ConnectionState.Connecting

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.wss(
                    host = RELAY_HOST,
                    port = RELAY_PORT,
                    path = RELAY_PATH
                ) {
                    session = this

                    val registerMsg = gson.toJson(
                        RegisterMessage(
                            sessionId = sessionId,
                            tokenHash = tokenHash,
                            mode = mode.name,
                            expiresAt = expiresAt
                        )
                    )
                    send(Frame.Text(registerMsg))

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            handleMessage(text)
                        }
                    }
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            } finally {
                session = null
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    suspend fun stopSharing() {
        val sessionId = currentSessionId ?: return

        try {
            val msg = gson.toJson(UnregisterMessage(sessionId = sessionId))
            session?.send(Frame.Text(msg))
        } catch (_: Exception) {
        }

        job?.cancel()
        session = null
        currentToken = null
        currentSessionId = null
        currentMode = null
        _connectionState.value = ConnectionState.Disconnected
    }

    suspend fun sendResponse(requestId: String, clientId: String, payload: String) {
        val msg = gson.toJson(
            ResponseMessage(
                requestId = requestId,
                clientId = clientId,
                payload = payload
            )
        )
        session?.send(Frame.Text(msg))
    }

    private suspend fun handleMessage(text: String) {
        val base = gson.fromJson(text, BaseMessage::class.java)

        when (base.type) {
            "REGISTER_OK" -> {
                _connectionState.value = ConnectionState.Connected
            }

            "FORWARD" -> {
                val forward = gson.fromJson(text, ForwardMessage::class.java)
                _incomingRequests.emit(forward)
            }

            "ERROR" -> {
                _connectionState.value = ConnectionState.Error("Registration failed")
            }
        }
    }
}

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
