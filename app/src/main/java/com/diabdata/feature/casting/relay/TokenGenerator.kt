package com.diabdata.feature.casting.relay

import com.diabdata.feature.casting.relay.TokenGenerator.TOKEN_LENGTH
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object TokenGenerator {

    private const val TOKEN_LENGTH = 6
    private const val CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    private val secureRandom = SecureRandom()

    /**
     * Generates a token with a random string of length [TOKEN_LENGTH]
     * and a prefix [mode].
     * Ex: "C-A7K92M4X8B" ou "M-X3P41BZ2N9"
     */
    fun generateToken(mode: ShareMode): String {
        val prefix = when (mode) {
            ShareMode.COMPANION -> "C"
            ShareMode.MEDICAL -> "M"
        }
        val random = buildString {
            repeat(TOKEN_LENGTH) {
                append(CHARS[secureRandom.nextInt(CHARS.length)])
            }
        }
        return "$prefix-$random"
    }

    /**
     * Hash SHA-256 of the token
     */
    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(token.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a unique session ID
     */
    fun generateSessionId(): String = UUID.randomUUID().toString()
}

enum class ShareMode {
    COMPANION,
    MEDICAL
}
