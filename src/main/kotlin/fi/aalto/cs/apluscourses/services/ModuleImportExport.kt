package fi.aalto.cs.apluscourses.services

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.platform.ide.progress.withModalProgress
import com.intellij.ui.dsl.builder.panel
import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.api.APlusApi
import fi.aalto.cs.apluscourses.model.component.Component
import fi.aalto.cs.apluscourses.model.component.Module
import fi.aalto.cs.apluscourses.model.people.Group
import fi.aalto.cs.apluscourses.model.people.User
import fi.aalto.cs.apluscourses.notifications.ModuleExportedNotification
import fi.aalto.cs.apluscourses.services.course.CourseManager
import fi.aalto.cs.apluscourses.ui.module.ExportModuleDialog
import fi.aalto.cs.apluscourses.utils.CoursesLogger
import fi.aalto.cs.apluscourses.utils.Version
import fi.aalto.cs.apluscourses.utils.ZipUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.PROJECT)
class ModuleImportExport(
    val project: Project,
    val cs: CoroutineScope
) {
    private val running = AtomicBoolean(false)

    fun importModules() {
        if (!running.compareAndSet(false, true)) {
            return
        }
        val modulesWithErrors = mutableListOf<String>()
        FileChooser.chooseFiles(
            FileChooserDescriptor(false, false, true, true, false, true).withExtensionFilter(
                "zip"
            ), project, null
        ) { files ->
            cs.launch {
                withModalProgress(project, message("ui.ModuleImportExport.import.progress")) {

                    files.forEach { file ->
                        try {
                            val desiredModuleName = file.nameWithoutExtension

                            val extractRoot = FileUtil.createTempDirectory("apluscourses", "modules")
                            ZipUtil.unzip(file.toNioPath().toFile(), extractRoot)

                            // Rename any *.iml that the course archive contained
                            extractRoot.walkTopDown()
                                .filter { it.isFile && it.extension == "iml" }
                                .forEach { iml ->
                                    val target = File(iml.parentFile, "$desiredModuleName.iml")
                                    if (!FileUtil.filesEqual(iml, target)) iml.renameTo(target)
                                }

                            // Create and load the module
                            val module = Module(
                                name = desiredModuleName,
                                zipUrl = "",
                                changelog = null,
                                latestVersion = Version.DEFAULT,
                                language = null,
                                project = project
                            )

                            extractRoot.copyRecursively(module.fullPath.toFile(), overwrite = true)

                            edtWriteAction {
                                if (module.updateAndGetStatus() == Component.Status.NOT_LOADED) {
                                    module.loadToProject()
                                }
                            }

                            module.downloadAndInstall()
                            CourseManager.getInstance(project)
                                .getMissingDependencies(module)
                                .forEach { it.downloadAndInstall() }

                            extractRoot.deleteRecursively()
                        } catch (e: Exception) {
                            CoursesLogger.error("Failed to import module", e)
                            modulesWithErrors.add(file.nameWithoutExtension)
                        }
                    }

                    CourseManager.getInstance(project).refreshModuleStatuses()
                }

                if (modulesWithErrors.isNotEmpty()) {
                    withContext(Dispatchers.EDT) {
                        DialogBuilder(project)
                            .title(message("ui.ModuleImportExport.import.error.title"))
                            .centerPanel(
                                panel {
                                    row {
                                        text(
                                            message(
                                                "ui.ModuleImportExport.import.error.content",
                                                modulesWithErrors.joinToString("") { "<li>$it</li>" })
                                        )
                                    }
                                }
                            )
                            .show()
                    }
                }
            }
        }

        running.set(false)
    }

    fun exportModule() {
        cs.launch {
            if (!running.compareAndSet(false, true)) {
                return@launch
            }
            exportModuleSuspend()
            running.set(false)
        }
    }

    private suspend fun exportModuleSuspend() {
        val (student, modules, groups) = withModalProgress(
            project,
            message("ui.ModuleImportExport.export.loading")
        ) {
            val course = CourseManager.getInstance(project).state.course ?: return@withModalProgress null

            val modules = ModuleManager.getInstance(project).modules
            if (modules.isEmpty()) return@withModalProgress null

            val groups = withContext(Dispatchers.IO) {
                listOf(Group.EXPORT_ALONE) + APlusApi.course(course).myGroups(project)
            }
            val student = APlusApi.me().get(project)

            Triple(student, modules, groups)
        } ?: return

        val dialog = withContext(Dispatchers.EDT) { ExportModuleDialog(project, modules.toList(), groups, student) }

        if (withContext(Dispatchers.EDT) { !dialog.showAndGet() }) {
            return
        }

        val module = dialog.getSelectedModule()
        val selectedGroup = dialog.getSelectedGroup()
        val outputPath = dialog.getOutputPath() ?: return
        val fileName = dialog.getFileName()

        withContext(Dispatchers.IO) {
            val moduleDir = module.guessModuleDir()?.toNioPathOrNull()?.toFile() ?: return@withContext

            val zipFile = outputPath.resolve("${fileName}.zip").toFile()

            val studentsInfo = createSubmittersInfo(student, selectedGroup)
            val additionalEntries = mapOf("students.txt" to studentsInfo.toByteArray())

            ZipUtil.zip(moduleDir, zipFile, additionalEntries)

            withContext(Dispatchers.EDT) {
                ModuleExportedNotification(module, zipFile).notify(project)
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
        if (selectedGroup != Group.SUBMIT_ALONE) {
            appendLine()
            selectedGroup.members.forEach { member ->
                appendLine("- ${member.name}")
            }
        }
    }
}
