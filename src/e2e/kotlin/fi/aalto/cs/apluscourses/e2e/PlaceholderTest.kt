package fi.aalto.cs.apluscourses.e2e

import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import fi.aalto.cs.apluscourses.e2e.fixtures.dialog
import fi.aalto.cs.apluscourses.e2e.fixtures.ideFrame
import fi.aalto.cs.apluscourses.e2e.steps.CommonSteps
import fi.aalto.cs.apluscourses.e2e.utils.StepLoggerInitializer
import fi.aalto.cs.apluscourses.e2e.utils.uiTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class PlaceholderTest {
  init {
    StepLoggerInitializer.init()
  }

  @Test
  fun mainTest() = uiTest {
    CommonSteps(this).createProject()
    CommonSteps(this).openAPlusProjectWindow()
    step("Cancel") {
      with(dialog("Select Course")) {
        button("Cancel").click()
      }
    }
    CommonSteps(this).openAPlusProjectWindow()
    step("Select course") {
      with(dialog("Select Course")) {
        findText("O1").click()
        button("OK").click()
      }
    }
    step("Choose settings") {
      CommonSteps(this).aPlusSettings(true)
    }
    step("Assertions") {
      with(ideFrame()) {
        with(projectViewTree()) {
          waitFor(
            Duration.ofSeconds(60),
            Duration.ofSeconds(1),
            "O1Library not found in project view tree"
          ) { hasText("O1Library") }
        }
        aPlusStripeButton().click()
        with(modules()) {
          waitFor(
            Duration.ofSeconds(60),
            Duration.ofSeconds(1),
            "O1Library not found in modules list"
          ) { hasText("O1Library") }
          assertTrue("A module is installed", hasText("  [Installed]"))
        }
      }
    }
  }
}
