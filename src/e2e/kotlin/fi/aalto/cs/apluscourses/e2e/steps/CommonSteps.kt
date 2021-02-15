package fi.aalto.cs.apluscourses.e2e.steps

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.step
import fi.aalto.cs.apluscourses.e2e.fixtures.*
import java.time.Duration

class CommonSteps(val remoteRobot: RemoteRobot) {

  fun createProject() {
    with (remoteRobot) {
      step("Create New Empty Project") {
        with(welcomeFrame()) {
          newProjectButton().click()
          with(dialog("New Project")) {
            findText("Empty Project").click()
            button("Next").click()
            button("Finish").click()
          }
        }
      }
      step("Add OpenJDK 11") {
        with(dialog("Project Structure", Duration.ofSeconds(20))) {
          sidePanel().findText("Project").click()
          customComboBox("\u001BProject SDK:").dropdown()
          with(heavyWeightWindow()) {
            findText("Add SDK").click()
            heavyWeightWindow().findText("Download JDK...").click()
          }
          with(dialog("Download JDK")) {
            customComboBox("Version:").click()
            heavyWeightWindow().findText("11").click()
            customComboBox("Vendor:").click()
            heavyWeightWindow().findText("AdoptOpenJDK (HotSpot)").click()
            button("Download").click()
          }
          button("OK").click()
        }
      }
    }
  }
}
