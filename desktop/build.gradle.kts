plugins {
    java
    application
}

import java.util.Properties

group = "com.middin.innovatie"
version = "0.9.6"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.middin.innovatie.desktop.Main")
    applicationName = "MiddinInnovatie"
}

val rootProps = Properties().apply {
    rootProject.file("gradle.properties").inputStream().use { load(it) }
}
val ghOwner = rootProps.getProperty("middin.github.owner", "").trim()
val ghRepo = rootProps.getProperty("middin.github.repo", "MiddinInnovatie").trim().ifBlank { "MiddinInnovatie" }
val ghBranch = rootProps.getProperty("middin.github.branch", "main").trim().ifBlank { "main" }
val updateFeedUrl = if (ghOwner.isNotEmpty() && !ghOwner.startsWith("YOUR_")) {
    "https://raw.githubusercontent.com/$ghOwner/$ghRepo/$ghBranch/releases/latest.json"
} else {
    ""
}

val appVersionOutDir = layout.buildDirectory.dir("generated/sources/appversion")

tasks.register("generateAppVersion") {
    val outDir = appVersionOutDir.get().asFile
    outputs.dir(outDir)
    doLast {
        val appGradle = rootProject.file("app/build.gradle.kts").readText()
        val versionName = Regex("""versionName\s*=\s*"([^"]+)"""").find(appGradle)?.groupValues?.get(1) ?: version.toString()
        val versionCode = Regex("""versionCode\s*=\s*(\d+)""").find(appGradle)?.groupValues?.get(1) ?: "11"
        val pkgDir = File(outDir, "com/middin/innovatie/desktop")
        pkgDir.mkdirs()
        File(pkgDir, "AppVersion.java").writeText(
            """
            package com.middin.innovatie.desktop;

            /** Auto-generated from gradle.properties + app/build.gradle.kts. */
            public final class AppVersion {
                public static final String NAME = "$versionName";
                public static final int CODE = $versionCode;

                public static final String UPDATE_FEED_DEFAULT =
                        "$updateFeedUrl";

                private AppVersion() {}
            }
            """.trimIndent() + "\n",
        )
    }
}

sourceSets.named("main") {
    java.srcDir(appVersionOutDir)
}

tasks.named("compileJava") {
    dependsOn("generateAppVersion")
}

tasks.register<Jar>("fatJar") {
    group = "distribution"
    description = "Runnable jar with all dependencies"
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.middin.innovatie.desktop.Main"
    }
    from(sourceSets.main.get().output)
    from(sourceSets.main.get().resources) {
        include("brand/**")
    }
    dependsOn("classes", "generateAppVersion")
}
