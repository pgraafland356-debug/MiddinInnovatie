package com.middin.innovatie.app

data class ChangelogItem(
    val version: String,
    val dateIso: String,
    val bulletsEn: List<String>,
)

/** Hand-maintained history; prepend [ChangelogRepository.buildMetadataItem] at runtime. */
object ChangelogData {
    /** [version] is the Android versionCode (build number) for that release. */
    val staticItems: List<ChangelogItem> = listOf(
        ChangelogItem(
            version = "8",
            dateIso = "2026-05-08",
            bulletsEn = listOf(
                "Changelog: the \"this APK\" card used versionName only; versionCode stayed in Gradle for update ordering.",
            ),
        ),
        ChangelogItem(
            version = "7",
            dateIso = "2026-05-07",
            bulletsEn = listOf(
                "Locale: MainActivity extends AppCompatActivity so per-app English/Dutch from settings applies to Compose stringResource (not stuck on system locale).",
            ),
        ),
        ChangelogItem(
            version = "6",
            dateIso = "2026-05-06",
            bulletsEn = listOf(
                "Language: English/Dutch applies app-wide from welcome, login, settings (stored preference + AppCompat locales); home news dates refresh when locale changes.",
                "Layout: adaptive padding (MiddinDimens), top app bar title ellipsizing + smaller logo on narrow screens, optional icon-only bottom nav on small width / large font scale.",
                "Gradle task checkAppLinks verifies RSS/fallback HTTPS URLs; build-config API/update URLs are warning-only.",
            ),
        ),
        ChangelogItem(
            version = "5",
            dateIso = "2026-04-22",
            bulletsEn = listOf(
                "Welcome: brand screen appears whenever you open the app while signed out (including returning from the background) and after sign-out — not only the first time.",
                "Login & welcome (edge-to-edge): DayNight activity theme, full-screen theme background, and status/navigation bar contrast that follows light vs dark — fixes dark mode on the sign-in screen.",
                "Offline product assistant: “who are you” style questions in Dutch (e.g. «wie je bent», «Kun jij me vertellen wie je bent?») and English («tell me who you are») now return the Productassistent self-description instead of a generic product miss.",
                "Local debug sign-in: added account CIV-demo (password demo) next to existing demo users.",
            ),
        ),
        ChangelogItem(
            version = "4",
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
            version = "3",
            dateIso = "2026-03-26",
            bulletsEn = listOf(
                "Muse (EEG headband) product description in catalog.",
                "Credits: Pieter - Bas Graafland, André de Winter, Safeer Khan (You).",
                "Changelog: top card shows this APK’s version and exact build time from Gradle (updates on every assembleDebug / assembleRelease).",
            ),
        ),
        ChangelogItem(
            version = "2",
            dateIso = "2026-03-24",
            bulletsEn = listOf(
                "Roadmap: home & branding, theme (system/light/dark), products + top 3, CameraX + ML Kit, Bluetooth list, Gemini, updates/info/about/credits, notifications + test.",
                "Debug local vs server sign-in toggle; mock API in local-api/; product catalog seed + Dutch descriptions; Sign out in app bar; delete product.",
                "Room migrations, stability tests, GitHub Actions workflow.",
            ),
        ),
        ChangelogItem(
            version = "1",
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
        val build = BuildConfig.VERSION_CODE.toString()
        return ChangelogItem(
            version = build,
            dateIso = dateOnly,
            bulletsEn = listOf(
                "Pre-1.0 beta: versionName is ${BuildConfig.VERSION_NAME} (see Settings for name + build). " +
                    "Changelog titles use this build number only. Built at $iso. " +
                    "Bump versionCode for each new APK; keep versionName below 1.0.0 until release. " +
                    "Edit ChangelogData.staticItems for human release notes.",
            ),
        )
    }
}
