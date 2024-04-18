import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.extensions.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    id("scala")
    id("jacoco")
    id("checkstyle")

    // ./gradle/libs.versions.toml
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(17)
}

// Configure project's dependencies
repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

dependencies {
    implementation(libs.annotations)
    implementation(libs.zip4j)
    implementation(libs.json)

    compileOnly(libs.scalaLibrary)

    testImplementation(libs.jupiterApi)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testRuntimeOnly(libs.junitJupiterEngine)

    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito)
    testImplementation(libs.restAssured) {
        exclude(group = "commons-codec", module = "commons-codec") // Excluded because of Cxeb68d52e-5509
    }

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))

        // Plugin Dependencies.
        // Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(properties("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies.
        // Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(properties("platformPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        testFramework(TestFrameworkType.Platform.JUnit4)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        version = properties("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = environment("INTELLIJ_PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels,
        // like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically.
        // Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
//        channels = properties("pluginVersion").map { listOf(it.substringAfter('-',
//        "").substringBefore('.').ifEmpty { "default"
//        }) }
        channels = listOf(System.getenv("INTELLIJ_PUBLISH_CHANNEL"))
    }

    verifyPlugin {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
//koverReport {
//    defaults {
//        xml {
//            onCheck = true
//        }
//    }
//}

abstract class GatherBuildInfoTask : DefaultTask() {
    @get:Input
    abstract val pluginVersion: Property<String>

    @get:Input
    abstract val courseVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun gatherBuildInfo() {
        outputDir.file("build-info.properties").get().asFile.writeText(
            """
            version=${pluginVersion.get()}
            courseVersion=${courseVersion.get()}
            """.trimIndent()
        )
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    test {
        useJUnitPlatform()
    }

    buildSearchableOptions {
        enabled = false // Disabled because it breaks dynamic reload
    }

    jacocoTestReport {
        reports.xml.required = true
    }

    register<GatherBuildInfoTask>("gatherBuildInfo") {
        pluginVersion = properties("pluginVersion").get()
        courseVersion = properties("courseVersion").get()
        outputDir = layout.buildDirectory.dir("resources/main")
    }

    classes {
        dependsOn("gatherBuildInfo")
    }

    check {
        dependsOn("jacocoTestReport")
    }
}

checkstyle {
    configFile = file("checkstyle/google_checks.xml")
    maxWarnings = 0
}
