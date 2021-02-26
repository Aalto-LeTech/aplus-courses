package fi.aalto.cs.apluscourses.e2e

import fi.aalto.cs.apluscourses.e2e.fixtures.ideFrame
import fi.aalto.cs.apluscourses.e2e.steps.CommonSteps
import fi.aalto.cs.apluscourses.e2e.utils.StepLoggerInitializer
import fi.aalto.cs.apluscourses.e2e.utils.uiTest
import org.junit.Assert.assertNotNull
import org.junit.Test

class PlaceholderTest {
  init {
    StepLoggerInitializer.init()
  }

  @Test
  fun simpleTest() = uiTest {
    CommonSteps(this).createProject()
    with(ideFrame()) {
      assertNotNull(menu().select("A+"))
    }
  }
}