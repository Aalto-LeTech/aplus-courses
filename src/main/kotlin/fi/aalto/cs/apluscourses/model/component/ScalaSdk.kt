package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.impl.libraries.LibraryEx.ModifiableModelEx
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.application
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState
import org.jetbrains.plugins.scala.project.ScalaLibraryType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.toTypedArray
import kotlin.io.path.isDirectory

class ScalaSdk(private val scalaVersion: String, project: Project) : Library(scalaVersion, project) {
    @NonNls
    private val versionNumber = scalaVersion.substringAfter("scala-sdk-")
    private val libPath = Path.of(project.basePath!!, ".libs")
    private val sdkPath = libPath.resolve("scala3-$versionNumber")

    override suspend fun downloadAndInstall(updating: Boolean) {
        status = Status.LOADING
        CoursesLogger.info("Downloading Scala SDK $scalaVersion")
        val zipUrl =
            "https://github.com/lampepfl/dotty/releases/download/$versionNumber/scala3-$versionNumber.zip"
        val path = libPath
        downloadAndUnzipZip(zipUrl, path)

        val libraryTable = libraryTable(project).modifiableModel
        libraryTable.getLibraryByName(scalaVersion)?.let {
            application.runWriteAction {
                libraryTable.removeLibrary(it)
            }
        }
        val kind: PersistentLibraryKind<ScalaLibraryProperties> = ScalaLibraryType.`Kind$`.`MODULE$`
        val library = libraryTable.createLibrary(name, kind)
        val compilerClasspath = sdkPath.toFile().walkTopDown()

        val libDir = sdkPath.resolve("lib")
        val m2Dir = sdkPath.resolve("maven2")
        val scala3Ver = versionNumber

        fun parseVersion(s: String) = s.split('.', '-', '_').mapNotNull { it.toIntOrNull() }.let {
            Triple(it.getOrElse(0) { 0 }, it.getOrElse(1) { 0 }, it.getOrElse(2) { 0 })
        }

        fun isScala38Plus(ver: String): Boolean {
            val (maj, min, _) = parseVersion(ver)
            return maj > 3 || (maj == 3 && min >= 8)
        }

        // Download REPL if scala ver >= 3.8.0
        // Unsure if actually required since this seems to be included as a dependency of the scala sdk and
        // found under the maven2 directory, but better be safe than sorry
        val replPath = libDir.resolve("scala3-repl_3-$scala3Ver.jar")
        var replClasspath = emptyArray<String>()
        if (isScala38Plus(scala3Ver)) {
            replClasspath =
                compilerClasspath.filter { it.extension == "jar" }.map { it.toString() }.toList().toTypedArray()
            val replUrl =
                "https://repo1.maven.org/maven2/org/scala-lang/scala3-repl_3/$scala3Ver/scala3-repl_3-$scala3Ver.jar"
            downloadFile(replUrl, replPath)
        }

        edtWriteAction {
            val libraryModel = library.modifiableModel

            // HACK: this is the only way to access properties that I am aware of
            val libraryEx = libraryModel as ModifiableModelEx
            val properties = libraryEx.properties
            val newState = ScalaLibraryPropertiesState(
                ScalaLanguageLevel.findByVersion(versionNumber).get(),
                getJarFiles({
                    VirtualFileManager.constructUrl(
                        LocalFileSystem.getInstance().protocol,
                        FileUtil.toSystemDependentName(it.toString())
                    )
                }).toTypedArray(), emptyArray<String>(), null, replClasspath
            )
            properties.loadState(newState)
            libraryEx.properties = properties

            libraryModel.commit()
            val newLibraryModel = library.modifiableModel
            newLibraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(compilerClasspath.find { it.name.startsWith("scala3-library") && it.extension == "jar" }!!),
                OrderRootType.CLASSES
            )
            newLibraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(compilerClasspath.find { it.name.startsWith("scala-library") && it.extension == "jar" }!!),
                OrderRootType.CLASSES
            )

            if (isScala38Plus(scala3Ver)) {
                newLibraryModel.addRoot(
                    VfsUtil.getUrlForLibraryRoot(compilerClasspath.find { it.name.startsWith("scala3-repl") && it.extension == "jar" }!!),
                    OrderRootType.CLASSES
                )
            }


            newLibraryModel.commit()
            libraryTable.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }

        val stdlibVer: String = if (isScala38Plus(scala3Ver)) {
            scala3Ver
        } else {
            fun findStdlibUnder(root: Path): String? {
                if (!Files.exists(root) || !root.isDirectory()) return null
                return Files.walk(root).use { stream ->
                    stream.filter { Files.isRegularFile(it) }
                        .map { it.fileName.toString() }
                        .filter { it.startsWith("scala-library-") && it.endsWith(".jar") }
                        .map { it.removeSuffix(".jar").substringAfter("scala-library-") }
                        .filter { it.startsWith("2.") }
                        .findFirst()
                        .orElse(null)
                }
            }
            findStdlibUnder(libDir)
                ?: findStdlibUnder(m2Dir)
                ?: error("scala-library 2.x jar not found under $libDir or $m2Dir for Scala $scala3Ver")
        }

        val scala3LibSources = libDir.resolve("scala3-library_3-$scala3Ver-sources.jar")
        downloadFile(
            "https://repo1.maven.org/maven2/org/scala-lang/scala3-library_3/$scala3Ver/scala3-library_3-$scala3Ver-sources.jar",
            scala3LibSources
        )

        val scalaStdlibSources = libDir.resolve("scala-library-$stdlibVer-sources.jar")
        downloadFile(
            "https://repo1.maven.org/maven2/org/scala-lang/scala-library/$stdlibVer/scala-library-$stdlibVer-sources.jar",
            scalaStdlibSources
        )


        // Attach sources jars to the library
        edtWriteAction {
            val libraryModel = library.modifiableModel
            fun addSourcesJar(path: Path) {
                if (Files.exists(path)) {
                    libraryModel.addRoot(VfsUtil.getUrlForLibraryRoot(path.toFile()), OrderRootType.SOURCES)
                }
            }
            addSourcesJar(scala3LibSources)
            addSourcesJar(scalaStdlibSources)
            libraryModel.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }

        status = Status.LOADED
    }

    private fun getJarFiles(pathToUri: (Path) -> String): List<String> {
        val files = sdkPath.toFile().walkTopDown()

        return files
            .filter { it.extension == "jar" && it.nameWithoutExtension != "scala-cli" }
            .map { pathToUri(it.toPath()) }
            .toList()
    }
}