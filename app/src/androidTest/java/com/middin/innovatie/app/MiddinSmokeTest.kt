package com.middin.innovatie.app

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiddinSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_starts() {
        composeRule.waitForIdle()
    }

    @Test
    fun app_shows_login_or_main_shell() {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            val signIn = composeRule.onAllNodes(hasText("Sign in")).fetchSemanticsNodes().isNotEmpty()
            val home = composeRule.onAllNodes(hasText("Home")).fetchSemanticsNodes().isNotEmpty()
            signIn || home
        }
        val signIn = composeRule.onAllNodes(hasText("Sign in")).fetchSemanticsNodes().isNotEmpty()
        val home = composeRule.onAllNodes(hasText("Home")).fetchSemanticsNodes().isNotEmpty()
        assertTrue("Expected Sign in (logged out) or Home tab (logged in)", signIn || home)
    }
}
