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
import fi.aalto.cs.apluscourses.model.people.User
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
                        val desiredModuleName = file.nameWithoutExtension

                        val zipFile = FileUtil.createTempDirectory("apluscourses", "modules")
                        zip.entries().asSequence().forEach { entry ->
                            val entryName = if (entry.name.endsWith(".iml")) {
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
        val (student, modules, groups) = withModalProgress(
            project,
            message("ui.ModuleImportExport.export.loading")
        ) {
            val course = CourseManager.getInstance(project).state.course ?: return@withModalProgress null

            val modules = ModuleManager.getInstance(project).modules
            if (modules.isEmpty()) return@withModalProgress null

            val groups = withContext(Dispatchers.IO) {
                listOf(Group.GROUP_ALONE) + APlusApi.course(course).myGroups(project)
            }
            val student = APlusApi.me().get(project) ?: return@withModalProgress null

            Triple(student, modules, groups)
        } ?: return

        val dialog = withContext(Dispatchers.EDT) { ExportModuleDialog(project, modules.toList(), groups, student) }

        if (withContext(Dispatchers.EDT) { !dialog.showAndGet() }) {
            return
        }

        val module = dialog.getSelectedModule()
        val selectedGroup = dialog.getSelectedGroup() ?: return
        val outputPath = dialog.getOutputPath() ?: return
        val fileName = dialog.getFileName()

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

                    val studentsInfo = createSubmittersInfo(student, selectedGroup)

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

    private fun createSubmittersInfo(submitter: User, selectedGroup: Group): String = buildString {
        appendLine(
            message(
                "ui.ModuleImportExport.export.submitter",
                submitter.userName,
                submitter.studentId ?: "???"
            )
        )
        if (selectedGroup != Group.GROUP_ALONE) {
            appendLine()
            selectedGroup.members.forEach { member ->
                appendLine("- ${member.name}")
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
