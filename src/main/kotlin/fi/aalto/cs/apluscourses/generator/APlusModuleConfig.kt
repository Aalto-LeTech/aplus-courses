package fi.aalto.cs.apluscourses.generator

import com.intellij.openapi.projectRoots.Sdk
import fi.aalto.cs.apluscourses.api.CourseConfig

class APlusModuleConfig {

    var programmingLanguage = ""
    var courseConfig: CourseConfig.JSON? = null
    var courseConfigUrl = ""
    var language = ""
    var importSettings = false
    var jdk: Sdk? = null
}

