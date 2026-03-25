package com.middin.innovatie.app

data class ChangelogItem(
    val version: String,
    val dateIso: String,
    val bulletsEn: List<String>,
)

class ChangelogRepository {
    val items: List<ChangelogItem> = listOf(
        ChangelogItem(
            version = "1.0.0",
            dateIso = "2026-03-24",
            bulletsEn = listOf(
                "All roadmap phases: home & branding, theme (system/light/dark), products with top 3, CameraX + ML Kit labels, Bluetooth paired devices, Gemini assistant, updates/info/about/credits, notifications channel + test.",
            ),
        ),
        ChangelogItem(
            version = "0.1.0",
            dateIso = "2026-03-24",
            bulletsEn = listOf(
                "Phase 1: Room collective memory, changelog, EN/NL, custom API login (Ktor) + bearer token, chat REST stubs, DataStore session, HTTPS baseline (cleartext only for dev hosts).",
            ),
        ),
    )
}
