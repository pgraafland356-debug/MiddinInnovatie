package com.middin.innovatie.app.data.remote

import com.google.ai.client.generativeai.GenerativeModel

class GeminiRepository {
    suspend fun generate(apiKey: String, userPrompt: String): Result<String> =
        runNetworkResult {
            val key = apiKey.trim()
            require(key.isNotEmpty()) { "Add your Gemini API key in Settings." }
            val prompt = userPrompt.trim()
            require(prompt.isNotEmpty()) { "Enter a prompt." }
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = key,
            )
            val response = model.generateContent(prompt)
            response.text?.trim()?.takeIf { it.isNotEmpty() }
                ?: error("Empty model response.")
        }
}
