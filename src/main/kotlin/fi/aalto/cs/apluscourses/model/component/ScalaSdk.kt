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
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarInputStream
import java.util.jar.Manifest
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

        val libDir = sdkPath.resolve("lib")
        val m2Dir = sdkPath.resolve("maven2")
        val scala3Ver = versionNumber

        val isScala38Plus = compareVersions(scala3Ver, "3.8.0") >= 0

        val fileTreeWalk = sdkPath.toFile().walkTopDown()

        val jars =
            if (isScala38Plus)
            // read classpath from JARs's manifests
                sequenceOf("scala.jar", "with_compiler.jar")
                    .map(sdkPath.resolve("lib")::resolve)
                    .flatMap(::getClassPathFromJar)
                    .map(FileUtil::toSystemDependentName)
                    .map(libDir::resolve)
                    .map(Path::normalize)
                    .map(Path::toString)
            else
            // just take (almost) everything
                fileTreeWalk.filter { it.extension == "jar" && it.nameWithoutExtension != "scala-cli" }
                    .map(File::toString)
                    .map(FileUtil::toSystemDependentName)

        val fileUrlProtocol = LocalFileSystem.getInstance().protocol

        val compilerClasspath = jars
            .map { VirtualFileManager.constructUrl(fileUrlProtocol, it) }
            .toList()
            .toTypedArray()

        val replClasspath = if (isScala38Plus) compilerClasspath else emptyArray()

        edtWriteAction {
            val libraryModel = library.modifiableModel

            // HACK: this is the only way to access properties that I am aware of
            val libraryEx = libraryModel as ModifiableModelEx
            val properties = libraryEx.properties
            val newState = ScalaLibraryPropertiesState(
                ScalaLanguageLevel.findByVersion(versionNumber).get(),
                compilerClasspath,
                emptyArray<String>(),
                null,
                replClasspath
            )
            properties.loadState(newState)
            libraryEx.properties = properties

            libraryModel.commit()
            val newLibraryModel = library.modifiableModel

            sequenceOf("scala3-library", "scala-library")
                .map { root -> fileTreeWalk.find { it.name.startsWith(root) && it.extension == "jar" }!! }
                .map(VfsUtil::getUrlForLibraryRoot)
                .forEach { newLibraryModel.addRoot(it,OrderRootType.CLASSES) }

            newLibraryModel.commit()
            libraryTable.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }

        val stdlibVer: String = if (isScala38Plus) {
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

    private fun parseVersion(s: String) = s.split('.', '-', '_').mapNotNull { it.toIntOrNull() }.let {
        Triple(it.getOrElse(0) { 0 }, it.getOrElse(1) { 0 }, it.getOrElse(2) { 0 })
    }

    private fun compareVersions(ver1: String, ver2: String): Int {
        val (maj1, min1, patch1) = parseVersion(ver1)
        val (maj2, min2, patch2) = parseVersion(ver2)
        if (maj1 != maj2) return maj1 - maj2
        if (min1 != min2) return min1 - min2
        return patch1 - patch2
    }

    private fun getClassPathFromJar(path: Path): List<String> = getClassPathFromJar(path.toFile())

    private fun getClassPathFromJar(file: File) = file.inputStream().use(::getClassPathFromJar)

    private fun getClassPathFromJar(inputStream: InputStream) = JarInputStream(inputStream).use(::getClassPathFromJar)

    private fun getClassPathFromJar(jarInputStream: JarInputStream) = getClassPathFromJar(jarInputStream.manifest)

    private fun getClassPathFromJar(manifest: Manifest) =
        manifest.mainAttributes.getValue("Class-Path")
            ?.split(' ')
            ?: emptyList()
}
