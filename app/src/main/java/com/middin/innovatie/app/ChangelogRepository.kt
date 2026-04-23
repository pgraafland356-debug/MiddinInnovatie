package com.middin.innovatie.app

data class ChangelogItem(
    val version: String,
    val dateIso: String,
    val bulletsEn: List<String>,
)

/** Hand-maintained history; prepend [ChangelogRepository.buildMetadataItem] at runtime. */
object ChangelogData {
    val staticItems: List<ChangelogItem> = listOf(
        ChangelogItem(
            version = "1.0.3",
            dateIso = "2026-04-22",
            bulletsEn = listOf(
                "Login & welcome (edge-to-edge): DayNight activity theme, full-screen theme background, and status/navigation bar contrast that follows light vs dark — fixes dark mode on the sign-in screen.",
                "Offline product assistant: “who are you” style questions in Dutch (e.g. «wie je bent», «Kun jij me vertellen wie je bent?») and English («tell me who you are») now return the Productassistent self-description instead of a generic product miss.",
                "Local debug sign-in: added account CIV-demo (password demo) next to existing demo users.",
            ),
        ),
        ChangelogItem(
            version = "1.0.2",
            dateIso = "2026-04-22",
            bulletsEn = listOf(
                "Collective memory: edit an existing note (dialog + save); entries still support delete.",
                "Product catalog: Qtronix Libra 90 USB trackball description; obsolete seed names cleaned up.",
                "Private updates: unknown-sources install flow simplified (minSdk 26+); Settings uses locale-aware strings for update status.",
                "Targets compileSdk / targetSdk 36; dependencies aligned on the version catalog (incl. CameraX, Ktor test mock, Room testing).",
                "Debug builds no longer ship preset sign-in credentials (empty by default).",
                "Launcher: adaptive icon wired via vector foreground/background under mipmap-anydpi (legacy webp mipmaps removed).",
                "Gradle: lower default heap / single worker for tighter machines; KSP incremental disabled as a workaround for a rare Windows classpath-snapshot crash.",
            ),
        ),
        ChangelogItem(
            version = "1.0.1",
            dateIso = "2026-03-26",
            bulletsEn = listOf(
                "Muse (EEG headband) product description in catalog.",
                "Credits: Pieter - Bas Graafland, André de Winter, Safeer Khan (You).",
                "Changelog: top card shows this APK’s version and exact build time from Gradle (updates on every assembleDebug / assembleRelease).",
            ),
        ),
        ChangelogItem(
            version = "1.0.0",
            dateIso = "2026-03-24",
            bulletsEn = listOf(
                "Roadmap: home & branding, theme (system/light/dark), products + top 3, CameraX + ML Kit, Bluetooth list, Gemini, updates/info/about/credits, notifications + test.",
                "Debug local vs server sign-in toggle; mock API in local-api/; product catalog seed + Dutch descriptions; Sign out in app bar; delete product.",
                "Room migrations, stability tests, GitHub Actions workflow.",
            ),
        ),
        ChangelogItem(
            version = "0.1.0",
            dateIso = "2026-03-24",
            bulletsEn = listOf(
                "Phase 1: collective memory, EN/NL, Ktor API login + chat, DataStore session, network security baseline.",
            ),
        ),
    )
}

class ChangelogRepository {
    val items: List<ChangelogItem>
        get() = listOf(buildMetadataItem()) + ChangelogData.staticItems

    private fun buildMetadataItem(): ChangelogItem {
        val iso = BuildConfig.BUILD_TIME_ISO
        val dateOnly = iso.take(10)
        return ChangelogItem(
            version = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            dateIso = dateOnly,
            bulletsEn = listOf(
                "This APK was built at $iso (timestamp from Gradle when this variant was configured). " +
                    "Bump versionName / versionCode in app/build.gradle.kts for store releases; edit ChangelogData.staticItems for human release notes.",
            ),
        )
    }
}
