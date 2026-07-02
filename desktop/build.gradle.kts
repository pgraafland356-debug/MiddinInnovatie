plugins {
    java
    application
}

group = "com.middin.innovatie"
version = "0.9.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.middin.innovatie.desktop.Main")
    applicationName = "MiddinInnovatie"
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
    dependsOn("classes")
}
