package com.middin.innovatie.app

data class UpdateItem(
    val title: String,
    val dateIso: String,
    val bulletsEn: List<String>,
)

class UpdatesRepository {
    val items: List<UpdateItem> = listOf(
        UpdateItem(
            title = "App 1.0.0",
            dateIso = "2026-03-24",
            bulletsEn = listOf(
                "Phases 1–4 baseline: home, products (top 3 + list), camera + ML labels, Bluetooth devices, Gemini assistant, updates & info, notifications test, theme & Middin Den Haag branding.",
            ),
        ),
    )
}
