package fi.aalto.cs.apluscourses.ui.overview

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import fi.aalto.cs.apluscourses.services.course.CourseFileManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

@TestApplication
class OverviewModelCacheTest {
    private val projectFixture = projectFixture()

    private fun state() = CourseFileManager.getInstance(projectFixture.get()).state

    @Test
    fun `remembered values are offered before the course arrives`() {
        val state = state()
        state.cachedBannerUrl = "https://plus.cs.aalto.fi/media/public/banner.png"
        state.cachedUserName = USER_NAME
        state.cachedCoursePageUrl = "https://plus.cs.aalto.fi/o1/2025/"

        val model = readOverviewModel(projectFixture.get())

        assertEquals("https://plus.cs.aalto.fi/media/public/banner.png", model.bannerUrl)
        assertEquals("Jaakko", model.userName)
        assertEquals("https://plus.cs.aalto.fi/o1/2025/", model.coursePageUrl)
    }

    @Test
    fun `an empty cache offers nothing`() {
        val state = state()
        state.cachedBannerUrl = null
        state.cachedUserName = null
        state.cachedCoursePageUrl = null

        val model = readOverviewModel(projectFixture.get())

        assertNull(model.bannerUrl)
        assertNull(model.userName)
        assertNull(model.coursePageUrl)
    }

    private companion object {
        @NonNls
        const val USER_NAME = "Jaakko"
    }
}
