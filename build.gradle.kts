plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register<Exec>("checkAppLinks") {
    group = "verification"
    description =
        "Verify HTTPS URLs in RSS/fallback Kotlin sources; warn-only checks for API_BASE_URL / UPDATE_FEED_URL in app/build.gradle.kts."
    workingDir = rootDir
    commandLine(
        "powershell",
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        file("${rootProject.projectDir}/scripts/check-app-links.ps1").absolutePath,
        "-RepoRoot",
        rootProject.projectDir.absolutePath,
    )
}
