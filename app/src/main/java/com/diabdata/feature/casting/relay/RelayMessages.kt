package com.diabdata.feature.casting.relay

data class RegisterMessage(
    val type: String = "REGISTER",
    val sessionId: String,
    val tokenHash: String,
    val mode: String,
    val expiresAt: Long
)

data class UnregisterMessage(val type: String = "UNREGISTER", val sessionId: String)

data class ResponseMessage(
    val type: String = "RESPONSE",
    val requestId: String,
    val clientId: String,
    val payload: String
)

data class RegisterOkMessage(val type: String = "REGISTER_OK", val sessionId: String)

data class ForwardMessage(
    val type: String = "FORWARD",
    val requestId: String,
    val clientId: String,
    val payload: String
)

data class SessionClosedMessage(val type: String = "SESSION_CLOSED", val reason: String)

data class BaseMessage(val type: String)
