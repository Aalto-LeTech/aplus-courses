package fi.aalto.cs.apluscourses.utils

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import org.jdom.Element
import org.jdom.JDOMException
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.nio.file.Path

/**
 * `.idea/externalDependencies.xml`: list of plugins required by the project.
 *
 * The platform reads this file on project open (`CheckProjectRequiredPluginsActivity`) and
 * prompts the user to install or enable what is missing.
 */
object RequiredPluginsFile {

    /** Replaces the required-plugins list with [pluginIds]. Does nothing when already identical. */
    suspend fun write(project: Project, pluginIds: Collection<String>) {
        val basePath = project.basePath ?: return
        val newContent = content(pluginIds)
        edtWriteAction {
            val ideaDir = VfsUtil.createDirectoryIfMissing(
                Path.of(basePath, Project.DIRECTORY_STORE_FOLDER).toString()
            ) ?: return@edtWriteAction
            val existing = ideaDir.findChild(FILE_NAME)
            if (existing != null && VfsUtil.loadText(existing) == newContent) return@edtWriteAction
            val file = existing ?: ideaDir.createChildData(this, FILE_NAME)
            VfsUtil.saveText(file, newContent)
        }
    }

    /** The required plugin ids, or an empty set when the file is missing or unreadable. */
    fun read(project: Project): Set<String> {
        val basePath = project.basePath ?: return emptySet()
        val file = LocalFileSystem.getInstance()
            .refreshAndFindFileByNioFile(Path.of(basePath, Project.DIRECTORY_STORE_FOLDER, FILE_NAME))
            ?: return emptySet()
        return try {
            pluginIds(VfsUtil.loadText(file))
        } catch (e: JDOMException) {
            CoursesLogger.warn("Could not parse $FILE_NAME", e)
            emptySet()
        } catch (e: IOException) {
            CoursesLogger.warn("Could not read $FILE_NAME", e)
            emptySet()
        }
    }

    /** The document in the platform's own storage format, with the ids sorted the way it sorts them. */
    internal fun content(pluginIds: Collection<String>): String {
        val component = Element(COMPONENT_TAG).setAttribute(NAME_ATTRIBUTE, COMPONENT_NAME)
        pluginIds.toSortedSet().forEach {
            component.addContent(Element(PLUGIN_TAG).setAttribute(ID_ATTRIBUTE, it))
        }
        val root = Element(PROJECT_TAG).setAttribute(VERSION_ATTRIBUTE, PROJECT_VERSION).addContent(component)
        @NonNls val document = "$XML_HEADER\n${JDOMUtil.write(root)}"
        return document
    }

    @Throws(JDOMException::class, IOException::class)
    internal fun pluginIds(@NonNls xml: String): Set<String> =
        JDOMUtil.load(xml)
            .getChildren(COMPONENT_TAG)
            .filter { it.getAttributeValue(NAME_ATTRIBUTE) == COMPONENT_NAME }
            .flatMap { it.getChildren(PLUGIN_TAG) }
            .mapNotNullTo(mutableSetOf()) { it.getAttributeValue(ID_ATTRIBUTE) }

    @NonNls
    private const val FILE_NAME = "externalDependencies.xml"

    @NonNls
    private const val XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"

    @NonNls
    private const val PROJECT_TAG = "project"

    @NonNls
    private const val VERSION_ATTRIBUTE = "version"

    @NonNls
    private const val PROJECT_VERSION = "4"

    @NonNls
    private const val COMPONENT_TAG = "component"

    @NonNls
    private const val NAME_ATTRIBUTE = "name"

    @NonNls
    private const val COMPONENT_NAME = "ExternalDependencies"

    @NonNls
    private const val PLUGIN_TAG = "plugin"

    @NonNls
    private const val ID_ATTRIBUTE = "id"
}
