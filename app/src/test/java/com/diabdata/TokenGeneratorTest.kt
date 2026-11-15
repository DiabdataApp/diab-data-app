package com.diabdata

import com.diabdata.feature.casting.relay.ShareMode
import com.diabdata.feature.casting.relay.TokenGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenGeneratorTest {
    // Token generation test
    @Test
    fun generateToken_companionMode_startsWithCPrefix() {
        assertTrue(
            TokenGenerator.generateToken(ShareMode.COMPANION).matches("C-[A-Z0-9]{6}".toRegex())
        )
    }

    @Test
    fun generateToken_medicalMode_startsWithMPrefix() {
        assertTrue(
            TokenGenerator.generateToken(ShareMode.MEDICAL).matches("M-[A-Z0-9]{6}".toRegex())
        )
    }

    @Test
    fun generateToken_companionMode_returnsEightChars() {
        assertEquals(8, TokenGenerator.generateToken(ShareMode.COMPANION).length)
    }

    @Test
    fun generateToken_medicalMode_returnsEightChars() {
        assertEquals(8, TokenGenerator.generateToken(ShareMode.MEDICAL).length)
    }

    @Test
    fun generateToken_companionMode_returnsNoCharactersOutsideChars() {
        assertTrue(
            !Regex("[IL10O]").containsMatchIn(TokenGenerator.generateToken(ShareMode.COMPANION))
        )
    }

    @Test
    fun generateToken_medicalMode_returnsNoCharactersOutsideChars() {
        assertTrue(
            !Regex("[IL10O]").containsMatchIn(TokenGenerator.generateToken(ShareMode.MEDICAL))
        )
    }

    @Test
    fun generateToken_companionMode_returnsUniqueToken() {
        assertNotEquals(
            TokenGenerator.generateToken(ShareMode.COMPANION),
            TokenGenerator.generateToken(ShareMode.COMPANION)
        )
    }

    @Test
    fun generateToken_medicalMode_returnsUniqueToken() {
        assertNotEquals(
            TokenGenerator.generateToken(ShareMode.MEDICAL),
            TokenGenerator.generateToken(ShareMode.MEDICAL)
        )
    }

    // Token hashing tests
    @Test
    fun hashToken_formatIsHex() {
        val testToken = TokenGenerator.generateToken(ShareMode.COMPANION)
        val hashedToken = TokenGenerator.hashToken(testToken)

        assertTrue(hashedToken.matches("[a-fA-F0-9]+".toRegex()))
    }

    @Test
    fun hashToken_tokenLengthIs64() {
        val testToken = TokenGenerator.generateToken(ShareMode.COMPANION)

        assertEquals(64, TokenGenerator.hashToken(testToken).length)
    }

    @Test
    fun hashToken_sameTokenReturnsSameHash() {
        val testToken = TokenGenerator.generateToken(ShareMode.COMPANION)

        assertEquals(TokenGenerator.hashToken(testToken), TokenGenerator.hashToken(testToken))
    }

    @Test
    fun hashToken_differentTokensReturnsDifferentHashes() {
        val testToken1 = TokenGenerator.generateToken(ShareMode.COMPANION)
        val testToken2 = TokenGenerator.generateToken(ShareMode.MEDICAL)

        assertNotEquals(TokenGenerator.hashToken(testToken1), TokenGenerator.hashToken(testToken2))
    }

    // SessionId generation test
    @Test
    fun generateSessionId_returnsUUID() {
        assertTrue(
            TokenGenerator.generateSessionId()
                .matches("[a-fA-F0-9]{8}-([a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}".toRegex())
        )
    }

    @Test
    fun generateSessionId_returnsDifferentIds() {
        assertNotEquals(TokenGenerator.generateSessionId(), TokenGenerator.generateSessionId())
    }
}
