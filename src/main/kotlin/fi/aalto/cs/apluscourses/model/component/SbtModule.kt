package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings.AlreadyImportedProjectException
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import fi.aalto.cs.apluscourses.utils.Version
import org.jetbrains.sbt.project.settings.SbtProjectSettings
import kotlin.io.path.invariantSeparatorsPathString

class SbtModule(
    name: String,
    zipUrl: String,
    changelog: String?,
    latestVersion: Version,
    language: String?,
    project: Project
) : Module(name, zipUrl, changelog, latestVersion, language, project) {

    override fun loadToProject() {
        val settings = SbtProjectSettings()
        settings.setupNewProjectDefault()
        settings.externalProjectPath = fullPath.invariantSeparatorsPathString
        val id = ProjectSystemId.findById("SBT") ?: return

        try {
            ExternalSystemApiUtil.getSettings(
                project,
                id
            ).linkProject(settings)
        } catch (ex: AlreadyImportedProjectException) {
            // this SBT module is already imported; a project-wide refresh is all that is required
        }

        FileDocumentManager.getInstance().saveAllDocuments()
        ExternalSystemUtil.refreshProjects(ImportSpecBuilder(project, id))
    }

    override fun waitForLoad() {
        val startTime = System.currentTimeMillis()
        while (platformObject == null && System.currentTimeMillis() - startTime < 300 * 1000) {
            Thread.sleep(1000)
        }
    }
}