package fi.aalto.cs.apluscourses.generator

import com.intellij.openapi.projectRoots.Sdk
import fi.aalto.cs.aplus.courseconfig.CourseConfig

class APlusModuleConfig {
    var programmingLanguage: String = ""
    var courseConfig: CourseConfig.Document? = null
    var courseConfigUrl: String = ""
    var language: String = ""
    var importSettings: Boolean = false
    var jdk: Sdk? = null
}

