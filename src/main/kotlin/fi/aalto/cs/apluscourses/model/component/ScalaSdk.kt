package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.scala.project.external.ScalaSdkUtils
import scala.Option
import scala.jdk.javaapi.CollectionConverters
import java.io.File
import java.nio.file.Path

class ScalaSdk(private val scalaVersion: String, project: Project) : Library(scalaVersion, project) {
    override suspend fun downloadAndInstall(updating: Boolean) {
        status = Status.LOADING
        CoursesLogger.info("Downloading Scala SDK $scalaVersion")
        val strippedScalaVersion = this.scalaVersion.substringAfter("scala-sdk-")
        val zipUrl =
            "https://github.com/lampepfl/dotty/releases/download/$strippedScalaVersion/scala3-$strippedScalaVersion.zip"
        val sourcesUrl = "https://github.com/scala/scala3/archive/refs/tags/$strippedScalaVersion.zip"
        val path = Path.of(project.basePath!!, ".libs")
        downloadAndUnzipZip(zipUrl, path)
        val libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project).modifiableModel
        libraryTable.getLibraryByName(scalaVersion)?.let {
            writeAction {
                libraryTable.removeLibrary(it)
            }
        }
        val provider = IdeModifiableModelsProviderImpl(project)
        val library = provider.createLibrary(scalaVersion)
        val compilerClasspath =
            path.resolve("scala3-$strippedScalaVersion").resolve("lib").toFile().listFiles().toList()
        val scala2Version =
            compilerClasspath.find { it.name.startsWith("scala-library") }?.nameWithoutExtension?.substringAfter("scala-library-")

        writeAction {
            val libraryModel = library.modifiableModel
            ScalaSdkUtils.ensureScalaLibraryIsConvertedToScalaSdk(
                provider,
                library,
                Option.apply(strippedScalaVersion.substringBeforeLast(".")),
                CollectionConverters.asScala(compilerClasspath).toSeq(),
                CollectionConverters.asScala(listOf<File>()).toSeq(),
                ScalaSdkUtils.resolveCompilerBridgeJar(strippedScalaVersion)
            )
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
            provider.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }
        runBlocking {
            val scala3SourcesPath = path.resolve("scala3-$strippedScalaVersion").resolve("src")
            val scala3Docs = async {
                downloadAndUnzipZip(
                    sourcesUrl,
                    scala3SourcesPath,
                    "scala3-$strippedScalaVersion/library/src/"
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
                    path.resolve("scala3-$strippedScalaVersion").resolve("src").resolve("scala3-$strippedScalaVersion")
                        .resolve("library").resolve("src")
                ),
                OrderRootType.SOURCES
            )
            libraryModel.addRoot(
                VfsUtil.getUrlForLibraryRoot(
                    path.resolve("scala3-$strippedScalaVersion").resolve("src").resolve("scala-$scala2Version")
                        .resolve("src").resolve("library")
                ),
                OrderRootType.SOURCES
            )
            libraryModel.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }
        status = Status.LOADED

    }
}