package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.impl.libraries.LibraryEx.ModifiableModelEx
import com.intellij.openapi.roots.libraries.PersistentLibraryKind
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.scala.project.ScalaLanguageLevel
import org.jetbrains.plugins.scala.project.ScalaLibraryProperties
import org.jetbrains.plugins.scala.project.ScalaLibraryPropertiesState
import org.jetbrains.plugins.scala.project.ScalaLibraryType
import java.nio.file.Path

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
        val sourcesUrl = "https://github.com/scala/scala3/archive/refs/tags/$versionNumber.zip"
        val path = libPath
        downloadAndUnzipZip(zipUrl, path)

        val libraryTable = libraryTable(project).modifiableModel
        libraryTable.getLibraryByName(scalaVersion)?.let {
            writeAction {
                libraryTable.removeLibrary(it)
            }
        }
        val kind: PersistentLibraryKind<ScalaLibraryProperties> = ScalaLibraryType.`Kind$`.`MODULE$`
        val library = libraryTable.createLibrary(name, kind)
        val compilerClasspath = sdkPath.resolve("lib").toFile().listFiles().toList()
        val scala2Version =
            compilerClasspath.find { it.name.startsWith("scala-library") }?.nameWithoutExtension?.substringAfter("scala-library-")

        writeAction {
            val libraryModel = library.modifiableModel

            // HACK: this is the only way to access properties that I am aware of
            val libraryEx = libraryModel as ModifiableModelEx
            val properties = libraryEx.properties
            val newState = ScalaLibraryPropertiesState(
                ScalaLanguageLevel.findByVersion(versionNumber).get(),
                getUris(getJarFiles()) {
                    VirtualFileManager.constructUrl(
                        LocalFileSystem.getInstance().protocol,
                        FileUtil.toSystemDependentName(it.toString())
                    )
                }.toTypedArray(), emptyArray<String>(), null
            )
            properties.loadState(newState)
            libraryEx.properties = properties

            libraryModel.commit()
            val newLibraryModel = library.modifiableModel
            newLibraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(compilerClasspath.find { it.name.startsWith("scala3-library") }!!),
                OrderRootType.CLASSES
            )
            newLibraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(compilerClasspath.find { it.name.startsWith("scala-library") }!!),
                OrderRootType.CLASSES
            )

            newLibraryModel.commit()
            libraryTable.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }
        runBlocking {
            val scala3SourcesPath = path.resolve("scala3-$versionNumber").resolve("src")
            val scala3Docs = async {
                downloadAndUnzipZip(
                    sourcesUrl,
                    scala3SourcesPath,
                    "scala3-$versionNumber/library/src/"
                )
            }
            val scala2Docs = async {
                val scala2SourcesUrl = "https://github.com/scala/scala/archive/refs/tags/v$scala2Version.zip"
                fullPath.resolve("src").resolve("scala-$scala2Version")
                downloadAndUnzipZip(scala2SourcesUrl, scala3SourcesPath, "scala-$scala2Version/src/library/")
            }
            scala3Docs.await()
            scala2Docs.await()
        }

        writeAction {
            val libraryModel = library.modifiableModel
            libraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(
                    path.resolve("scala3-$versionNumber").resolve("src").resolve("scala3-$versionNumber")
                        .resolve("library").resolve("src")
                ),
                OrderRootType.SOURCES
            )
            libraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(
                    path.resolve("scala3-$versionNumber").resolve("src").resolve("scala-$scala2Version")
                        .resolve("src").resolve("library")
                ),
                OrderRootType.SOURCES
            )
            libraryModel.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }
        status = Status.LOADED

    }

    private fun getUris(roots: List<String>, pathToUri: (Path) -> String): List<String> {
        return roots
            .filter { it.isNotEmpty() }
            .map { sdkPath.resolve("lib").resolve(it) }
            .map { pathToUri(it) }
    }

    private fun getJarFiles(): List<String> {
        val files = sdkPath.resolve("lib").toFile().listFiles()
        return files
            .map { it.name }
            .filter { it.endsWith(".jar") }
    }
}