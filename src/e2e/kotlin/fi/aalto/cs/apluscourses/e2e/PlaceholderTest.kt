package fi.aalto.cs.apluscourses.e2e

import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import fi.aalto.cs.apluscourses.e2e.fixtures.dialog
import fi.aalto.cs.apluscourses.e2e.fixtures.ideFrame
import fi.aalto.cs.apluscourses.e2e.steps.CommonSteps
import fi.aalto.cs.apluscourses.e2e.utils.StepLoggerInitializer
import fi.aalto.cs.apluscourses.e2e.utils.uiTest
import org.junit.Assert.assertEquals
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
    step("Searching modules") {
      with(ideFrame()) {
        with(modules()) {
          waitFor(
            Duration.ofSeconds(60),
            Duration.ofSeconds(1),
            "Viinaharava not found in modules list"
          ) { hasText("Viinaharava") }
          click()
          keyboard {
            enterText("viina")
          }
          assertEquals("Only one item is selected", selectedItems.size, 1)
          assertTrue("Selected module is correct", selectedItems[0].contains("Viinaharava", true))
          keyboard {
            escape()
            enterText("'") // a character that won't match anything
          }
          assertEquals("Nothing should be selected", selectedItems.size, 0)
        }
      }
    }
  }
}
