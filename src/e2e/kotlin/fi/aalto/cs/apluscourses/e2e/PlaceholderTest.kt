package fi.aalto.cs.apluscourses.e2e

import fi.aalto.cs.apluscourses.e2e.fixtures.*
import fi.aalto.cs.apluscourses.e2e.steps.CommonSteps
import fi.aalto.cs.apluscourses.e2e.utils.StepLoggerInitializer
import fi.aalto.cs.apluscourses.e2e.utils.uiTest
import org.junit.Assert.*
import org.junit.Test

class PlaceholderTest {
  init {
    StepLoggerInitializer.init()
  }

  @Test
  fun simpleTest() = uiTest {
    with(CommonSteps(this)) {
      createProject()
      closeTipOfTheDay()
      downloadAndSetOpenJdk11()
    }
    with(ideFrame()) {
      assertNotNull(menu().select("A+"))
    }
  }
}
