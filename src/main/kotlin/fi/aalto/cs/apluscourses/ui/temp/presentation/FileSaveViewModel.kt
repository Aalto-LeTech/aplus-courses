package fi.aalto.cs.apluscourses.ui.temp.presentation

import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

class FileSaveViewModel(
    title: String,
    description: String,
    defaultDirectory: VirtualFile?,
    defaultName: String?
) {
    private val title: String

    private val description: String

    private val defaultDirectory: VirtualFile?

    private val defaultName: String?

    private var path: Path? = null

    /**
     * Construct a view model with the given parameters.
     *
     * @param defaultDirectory An optional default directory.
     * @param defaultName      An optional default name for the file.
     */
    init {
        this.title = title
        this.description = description
        this.defaultDirectory = defaultDirectory
        this.defaultName = defaultName
    }

    fun getTitle(): String {
        return title
    }

    fun getDescription(): String {
        return description
    }

    fun getDefaultDirectory(): VirtualFile? {
        return defaultDirectory
    }

    fun getDefaultName(): String? {
        return defaultName
    }

    fun getPath(): Path {
        return path!!
    }

    fun setPath(path: Path) {
        this.path = path
    }
}
