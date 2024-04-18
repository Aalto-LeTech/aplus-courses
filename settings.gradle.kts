// Use IntelliJ Platform Gradle Plugin snapshot repository
pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "1.9.23"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "A+ Courses"
