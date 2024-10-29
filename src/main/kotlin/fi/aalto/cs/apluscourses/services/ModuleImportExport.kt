package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.ide.progress.withModalProgress
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.notifications.ModuleExportedNotification
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.module.ExportModuleDialog
import fi.aalto.cs.apluscourses.utils.Version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@Service(Service.Level.PROJECT)
class ModuleImportExport(
    val project: Project,
    val cs: CoroutineScope
) {
    fun importModules() {
        FileChooser.chooseFiles(FileChooserDescriptorImpl(), project, null) { files ->
            cs.launch {
                withModalProgress(project, message("ui.ModuleImportExport.import.progress")) {
                    files.forEach { file ->
                        val zip = ZipFile(file.toNioPath().toFile())
                        val imlEntry = zip.entries()
                            .asSequence()
                            .find { it.name.endsWith(".iml") }

                        val desiredModuleName = file.nameWithoutExtension

                        val zipFile = FileUtil.createTempDirectory("apluscourses", "modules")
                        zip.entries().asSequence().forEach { entry ->
                            val entryName = if (entry.name == imlEntry?.name) {
                                "$desiredModuleName.iml"
                            } else {
                                entry.name
                            }
                            val entryFile = zipFile.resolve(entryName)

                            if (entry.isDirectory) {
                                entryFile.mkdirs()
                            } else {
                                entryFile.parentFile.mkdirs()
                                entryFile.writeBytes(zip.getInputStream(entry).readBytes())

                            }
                        }

                        // Create and load module
                        val module = Module(
                            name = desiredModuleName,
                            zipUrl = "",
                            changelog = null,
                            latestVersion = Version.DEFAULT,
                            language = null,
                            project = project
                        )

                        val moduleDir = module.fullPath.toFile()
                        zipFile.copyRecursively(moduleDir, overwrite = true)

                        withContext(Dispatchers.EDT) {
                            if (module.updateAndGetStatus() == Component.Status.NOT_LOADED) {
                                module.loadToProject()
                            }
                        }

                        module.downloadAndInstall()
                        val missingDependencies = CourseManager.getInstance(project).getMissingDependencies(module)
                        missingDependencies.forEach { it.downloadAndInstall() }

                        zipFile.deleteRecursively()
                    }

                    CourseManager.getInstance(project).refreshModuleStatuses()
                }
            }
        }
    }

    fun exportModule() {
        cs.launch {
            exportModuleSuspend()
        }
    }

    suspend fun exportModuleSuspend() {
        val course = CourseManager.getInstance(project).state.course ?: return
        val modules = ModuleManager.getInstance(project).modules
        if (modules.isEmpty()) return

        val groups = withContext(Dispatchers.IO) {
            listOf(Group.GROUP_ALONE) + APlusApi.course(course).myGroups(project)
        }
        val student = APlusApi.me().get(project)
        if (student == null) {
            return
        }

        val dialog = withContext(Dispatchers.EDT) {
            val dialog = ExportModuleDialog(project, modules.toList(), groups, student)
            dialog.showAndGet() to dialog
        }

        if (!dialog.first) return

        val module = dialog.second.getSelectedModule()
        val selectedGroup = dialog.second.getSelectedGroup() ?: return
        val outputPath = dialog.second.getOutputPath() ?: return
        val fileName = dialog.second.getFileName()

        withContext(Dispatchers.IO) {
            val moduleDir = module.guessModuleDir()?.toNioPathOrNull()?.toFile()
            if (moduleDir == null) {
                return@withContext
            }
            val zipFile = outputPath.resolve("${fileName}.zip").toFile()
            if (zipFile.exists()) {
                zipFile.delete()
            }

            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->

                    // Text file containing info about submitters
                    val studentsInfo = buildString {
                        appendLine(
                            message(
                                "ui.ModuleImportExport.export.submitter",
                                student.userName,
                                student.studentId ?: "???"
                            )
                        )
                        if (selectedGroup != Group.GROUP_ALONE) {
                            appendLine()
                            selectedGroup.members.forEach { member ->
                                appendLine("- ${member.name}")
                            }
                        }
                    }

                    zos.putNextEntry(ZipEntry("students.txt"))
                    zos.write(studentsInfo.toByteArray())
                    zos.closeEntry()

                    moduleDir.walkTopDown()
                        .forEach { file ->
                            val zipEntry = ZipEntry(
                                file.relativeTo(moduleDir).path +
                                        (if (file.isDirectory) "/" else "")
                            )
                            zos.putNextEntry(zipEntry)
                            if (file.isFile) {
                                file.inputStream().use { it.copyTo(zos) }
                            }
                            zos.closeEntry()
                        }
                }

                withContext(Dispatchers.EDT) {
                    ModuleExportedNotification(module, zipFile).notify(project)
                }
            }
        }
    }

    private class FileChooserDescriptorImpl() :
        FileChooserDescriptor(false, false, true, true, false, true) {

        init {
            title = message("ui.ModuleImportExport.import.title")
            description = message("ui.ModuleImportExport.import.description")
        }

        override fun isFileSelectable(file: VirtualFile?): Boolean {
            if (file == null) {
                return false
            }

            val extension = file.getExtension()
            return Comparing.strEqual(extension, "zip")
        }
    }
}
