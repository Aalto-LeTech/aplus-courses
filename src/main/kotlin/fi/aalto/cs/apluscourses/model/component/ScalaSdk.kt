package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.scala.project.external.ScalaSdkUtils
import scala.Option
import scala.jdk.javaapi.CollectionConverters
import java.io.File
import java.nio.file.Path

class ScalaSdk(private val scalaVersion: String, project: Project) : Library(scalaVersion, project) {
    override suspend fun downloadAndInstall(updating: Boolean) {
        println("Downloading Scala SDK $scalaVersion")
        val strippedScalaVersion = this.scalaVersion.substringAfter("scala-sdk-")
        val zipUrl =
            "https://github.com/lampepfl/dotty/releases/download/$strippedScalaVersion/scala3-$strippedScalaVersion.zip"
        val sourcesUrl = "https://github.com/scala/scala3/archive/refs/tags/$strippedScalaVersion.zip"
        val path = Path.of(project.basePath!!, ".libs")
        downloadAndUnzipZip(zipUrl, path) // TODO only "scala3-$scalaVersion/lib"?
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
        println("compiler classpath $compilerClasspath")
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
            async { downloadAndUnzipZip(sourcesUrl, scala3SourcesPath, "scala3-$strippedScalaVersion/library/src/") }
            async {
                val scala2SourcesUrl = "https://github.com/scala/scala/archive/refs/tags/v$scala2Version.zip"
                val scala2SourcesPath = fullPath.resolve("src").resolve("scala-$scala2Version")
                downloadAndUnzipZip(scala2SourcesUrl, scala3SourcesPath, "scala-$scala2Version/src/library/")
            }
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

    }
//
//        application.invokeLater {
//            val a = ProjectStructureConfigurable(project)
//            val b = ProjectLibrariesConfigurable(a)
////            val f = StructureConfigurableContext()
//            project.service<IdeaModifiableModelsProvider>()
//            b.init(a.context)
//            ConvertProjectLibraryToRepositoryLibraryAction(b, b.conte)
//            CreateNewLibraryAction.createLibrary(
//                ScalaLibraryType.apply(),
//                b.tree,
//                project,
//                (f.getLibraryTableModifiableModel(project) as LibraryTableModifiableModelProvider)!!.modifiableModel
//                a.context.createModifiableModelProvider(LibraryTablesRegistrar.PROJECT_LEVEL).modifiableModel
//                b.modelProvider.modifiableModel
//                LibraryTablesRegistrar.getInstance().getLibraryTable(project).modifiableModel
//            )
//        }
//        application.invokeLater {
//            ScalaLibraryType.Description.createNewLibrary(null, project.baseDir)
//            LibraryTableId.ProjectLibraryTableId
//            val dialog = SdkSelectionDialogWrapper(project.baseDir)
//            dialog.createLeftSideActions()[0].actionPerformed(null)
//        }

//        val resolved = ScalaVersionDownloadingDialog.createScalaVersionResolveResult(version.get(), dependencyManager)
//        ScalaLibraryType()
//        LibraryDownloadSettings(
//            version,
//            ,
//            LibrariesContainer.LibraryLevel.PROJECT,
//            ""
//        )
//        type.createNewLibrary(project, resolved)
//        ScalaLibraryType.Description.createNewLibraryWithDefaultSettings()
//        ScalaLibraryType.Description.createNewLibrary(project.
//        println("scala resolved $resolved")
//    }

    override suspend fun remove(deleteFiles: Boolean) {
        TODO("Not yet implemented")
    }
}