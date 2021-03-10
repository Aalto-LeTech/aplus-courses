package fi.aalto.cs.apluscourses.e2e

import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import fi.aalto.cs.apluscourses.e2e.fixtures.dialog
import fi.aalto.cs.apluscourses.e2e.fixtures.ideFrame
import fi.aalto.cs.apluscourses.e2e.steps.CommonSteps
import fi.aalto.cs.apluscourses.e2e.utils.StepLoggerInitializer
import fi.aalto.cs.apluscourses.e2e.utils.getVersion
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
        //step 2
        CommonSteps(this).createProject()
        //step 4
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
                aPlusSideBarButton().click()
                with(modules()) {
                    waitFor(
                        Duration.ofSeconds(60),
                        Duration.ofSeconds(1),
                        "No modules installed in modules list"
                    ) { hasText { textData -> textData.text.contains("[Installed]") } }
                    assertTrue(
                        "O1Library is in the modules tree",
                        hasText("O1Library")
                    )
                }
            }
        }
        //step 8
        step("Authenticate with empty token") {
            CommonSteps(this).setAPlusToken("")
            with(dialog("A+ Token")) {
                assertTrue("Token dialog still showing after clicking OK with empty token", isShowing)
                button("Cancel").click()
            }
        }
        step("Authenticate with right token") {
            CommonSteps(this).setAPlusToken(System.getenv("APLUS_TEST_TOKEN"))
            with(ideFrame()) {
                with(assignments()) {
                    waitFor(
                        Duration.ofSeconds(60),
                        Duration.ofSeconds(1),
                        "Week 1 wasn't found in the assignments tree"
                    ) { hasText("Week 1") }
                }
            }
        }
    }
  

    @Test
    fun aboutDialogTest() = uiTest {
      step("About dialog") {
        CommonSteps(this).openAboutDialog()
        with(dialog("A+ Courses")) {
          step("Check the version") {
            assertTrue("Version is correct", hasText("Version: ${getVersion()}"))
          }
          step("Check the links") {
            assertTrue(hasText("A+ Courses plugin website"))
            assertTrue(hasText("A+ Courses plugin GitHub"))
            assertTrue(hasText("A+ website"))
            assertTrue(hasText("Apache Commons IO"))
            assertTrue(hasText("IntelliJ Scala Plugin"))
            assertTrue(hasText("json.org"))
            assertTrue(hasText("Scala Standard Library 2.13.4"))
            assertTrue(hasText("zip4j"))
          }
          button("OK").click()
        }
      }
    }
}
