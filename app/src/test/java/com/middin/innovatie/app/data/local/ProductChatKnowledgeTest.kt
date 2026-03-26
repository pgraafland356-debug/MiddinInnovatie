package com.middin.innovatie.app.data.local

import org.junit.Assert.assertTrue
import org.junit.Test

class ProductChatKnowledgeTest {

    private val somnox = Product(
        name = "De Somnox",
        description = "Zachte slaaprobot. Helpt met inslapen.",
        imageUri = null,
    )

    @Test
    fun mentionsSomnoxForSomnoxQuestion() {
        val a = ProductChatKnowledge.answer("Wat is de Somnox?", listOf(somnox))
        assertTrue(a.contains("Somnox", ignoreCase = true))
        assertTrue(a.contains("slaap", ignoreCase = true))
    }

    @Test
    fun listsProductsWhenAsked() {
        val a = ProductChatKnowledge.answer("Welke producten zijn er?", listOf(somnox))
        assertTrue(a.contains("Somnox", ignoreCase = true))
        assertTrue(a.contains("•"))
    }

    @Test
    fun englishListQuestionStillAnswersInDutch() {
        val a = ProductChatKnowledge.answer("Which products do you have?", listOf(somnox))
        assertTrue(a.contains("Dit staat er nu in de app", ignoreCase = true))
        assertTrue(a.contains("Somnox", ignoreCase = true))
    }

    @Test
    fun greetingDoesNotCrash() {
        val a = ProductChatKnowledge.answer("Hallo!", listOf(somnox))
        assertTrue(a.contains("productassistent", ignoreCase = true))
    }
}
