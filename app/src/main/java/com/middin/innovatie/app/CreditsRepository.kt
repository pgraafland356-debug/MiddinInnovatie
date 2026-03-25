package com.middin.innovatie.app

data class CreditEntry(
    val name: String,
    val roleEn: String,
)

object CreditsRepository {
    val entries: List<CreditEntry> = listOf(
        CreditEntry("Middin Innovatie team", "Product & innovation — Den Haag"),
        CreditEntry("Platform engineering", "Android, API integration, security"),
        CreditEntry("You", "Add names in CreditsRepository.kt"),
    )
}
