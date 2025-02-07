package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class InitializationStatus {
    var isNotCourse: Boolean = false
    var isIoError: Boolean = false

    companion object {
        fun isNotCourse(project: Project): Boolean = project.service<InitializationStatus>().isNotCourse
        fun isIoError(project: Project): Boolean = project.service<InitializationStatus>().isIoError

        fun setIsNotCourse(project: Project) {
            project.service<InitializationStatus>().isNotCourse = true
        }

        fun setIsIoError(project: Project) {
            project.service<InitializationStatus>().isIoError = true
        }
    }
}