package com.middin.innovatie.app.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginResponseTest {
    @Test
    fun bearerToken_prefersToken() {
        assertEquals(
            "a",
            LoginResponse(token = "a", accessToken = "b", access_token = "c").bearerToken(),
        )
    }

    @Test
    fun bearerToken_fallsBackToAccessToken() {
        assertEquals("b", LoginResponse(accessToken = "b").bearerToken())
    }

    @Test
    fun bearerToken_fallsBackToSnakeCase() {
        assertEquals("c", LoginResponse(access_token = "c").bearerToken())
    }

    @Test
    fun bearerToken_nullWhenMissing() {
        assertNull(LoginResponse().bearerToken())
    }
}
