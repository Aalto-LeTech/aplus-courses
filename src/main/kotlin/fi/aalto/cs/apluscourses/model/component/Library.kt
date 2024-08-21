package fi.aalto.cs.apluscourses.model.component

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar
import java.nio.file.Path
import com.intellij.openapi.roots.libraries.Library as IdeaLibrary

abstract class Library(name: String, project: Project) : Component<IdeaLibrary>(name, project) {
    override val platformObject: IdeaLibrary?
        get() = libraryTable(project).getLibraryByName(name)

    override val path: Path
        get() = Path.of(".libs", name)

    override val fullPath: Path
        get() = Path.of(project.basePath!!).resolve(path)

    override fun findDependencies(): Set<String> = emptySet()

    override fun updateStatus() {
        if (status == Status.LOADING) return
        status = if (platformObject != null) {
            Status.LOADED
        } else {
            Status.UNRESOLVED
        }
    }

    companion object {
        fun libraryTable(project: Project): LibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project)
    }
}