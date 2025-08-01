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
            application.runWriteAction {
                libraryTable.removeLibrary(it)
            }
        }
        val kind: PersistentLibraryKind<ScalaLibraryProperties> = ScalaLibraryType.`Kind$`.`MODULE$`
        val library = libraryTable.createLibrary(name, kind)
        val compilerClasspath = sdkPath.toFile().walkTopDown()

        val scala2Version =
            compilerClasspath.find { it.name.startsWith("scala-library") && it.extension == "jar" }?.nameWithoutExtension?.substringAfter(
                "scala-library-"
            )

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
                }).toTypedArray(), emptyArray<String>(), null
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

            newLibraryModel.commit()
            libraryTable.commit()
            VirtualFileManager.getInstance().syncRefresh()
        }


        val scala3SourcesPath = path.resolve("scala3-$versionNumber").resolve("src")
        downloadAndUnzipZip(
            sourcesUrl,
            scala3SourcesPath,
            "scala3-$versionNumber/library/src/"
        )


        val scala2SourcesUrl = "https://github.com/scala/scala/archive/refs/tags/v$scala2Version.zip"
        fullPath.resolve("src").resolve("scala-$scala2Version")
        downloadAndUnzipZip(scala2SourcesUrl, scala3SourcesPath, "scala-$scala2Version/src/library/")

        edtWriteAction {
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

    private fun getJarFiles(pathToUri: (Path) -> String): List<String> {
        val files = sdkPath.toFile().walkTopDown()

        return files
            .filter { it.extension == "jar" && it.nameWithoutExtension != "scala-cli" }
            .map { pathToUri(it.toPath()) }
            .toList()
    }
}