package com.middin.innovatie.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiUrlTest {
    @Test
    fun joinApiPath_noPrefix() {
        assertEquals(
            "https://api.example.com/auth/login",
            joinApiPath("https://api.example.com/", "", "auth/login"),
        )
    }

    @Test
    fun joinApiPath_withPrefix() {
        assertEquals(
            "https://api.example.com/api/v1/chat/messages",
            joinApiPath("https://api.example.com", "api/v1", "chat/messages"),
        )
    }

    @Test
    fun trimApiBaseUrl_stripsTrailingSlashes() {
        assertEquals("http://10.0.2.2:8080", "http://10.0.2.2:8080///".trimApiBaseUrl())
    }
}
