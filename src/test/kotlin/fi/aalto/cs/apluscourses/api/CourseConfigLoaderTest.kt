package fi.aalto.cs.apluscourses.api

import fi.aalto.cs.aplus.courseconfig.CourseConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CourseConfigLoaderTest {
    @Test
    fun `reads shared manifests with student-created workspaces and unknown fields`() {
        val config: CourseConfig.Document = CourseConfigLoader.deserialize(
            """{
                "id":"1","name":"Course","aPlusUrl":"https://plus.cs.aalto.fi/","languages":["fi","en"],
                "modules":[{"name":"Library","url":"https://example.test/library.zip","version":"1.2"}],
                "exerciseModules":{"42":{"fi":"Ave","en":"Ave"}},"future":{"enabled":true}
            }"""
        )
        assertEquals("1.2", config.modules.single().version)
        assertEquals("Ave", config.exerciseModules[42L]?.get("fi"))
    }

    @Test
    fun `plugin requires IDE configuration fields that template consumers may omit`() {
        for (fields in listOf("", "\"aPlusUrl\":\"https://plus.cs.aalto.fi/\",", "\"languages\":[\"en\"],")) {
            assertThrows(IllegalArgumentException::class.java) {
                CourseConfigLoader.deserialize("""{"id":"1","name":"Course",$fields"modules":[],"exerciseModules":{}}""")
            }
        }
    }

    @Test
    fun `all resource URLs survive manifest loading`() {
        val resources = mapOf(
            "ideSettings" to "https://example.test/default.zip",
            "ideSettingsMac" to "https://example.test/mac.zip",
            "ideSettingsLinux" to "https://example.test/linux.zip",
            "ideSettingsWindows" to "https://example.test/windows.zip",
            "projectSettings" to "https://example.test/project.zip",
            "customProperties" to "https://example.test/course.properties",
            "feedbackCss" to "https://example.test/feedback.css",
        )
        val resourceJson = resources.entries.joinToString(",") { (key, url) -> "\"$key\":\"$url\"" }
        val config = CourseConfigLoader.deserialize(
            """{"id":"1","name":"Course","aPlusUrl":"https://plus.cs.aalto.fi/","languages":["en"],
                "modules":[],"exerciseModules":{},"resources":{$resourceJson}}"""
        )
        assertEquals(resources, CourseConfigLoader.resourceUrls(config.resources).mapValues { it.value.toString() })
        assertEquals(emptyMap<String, String>(), CourseConfigLoader.resourceUrls(null))
    }

}
