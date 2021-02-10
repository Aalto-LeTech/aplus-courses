package fi.aalto.cs.apluscourses.e2e.steps

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.step
import fi.aalto.cs.apluscourses.e2e.fixtures.*
import fi.aalto.cs.apluscourses.e2e.utils.wait
import java.time.Duration

class CommonSteps(val remoteRobot: RemoteRobot) {

  fun createProject() = step("Create New Empty Project") {
    with(remoteRobot) {
      with(welcomeFrame()) {
        button("New Project").click()
        with(dialog("New Project")) {
          findText("Empty Project").click()
          button("Next").click()
          button("Finish").click()
        }
      }
      wait(Duration.ofSeconds(10))
      ideFrame().allDialogs().forEach { it.close() }
    }
  }

  fun downloadAndSetOpenJdk11() = step("Add OpenJDK 11") {
    with(remoteRobot.ideFrame()) {
      menu().select("File").select("Project Structure...")
      with(dialog("Project Structure")) {
        sidePanel().findText("Project").click()
        comboBox("\u001BProject SDK:").dropdown()
        with(heavyWeightWindow()) {
          findText("Add SDK").click()
          heavyWeightWindow().findText("Download JDK...").click()
        }
        with(dialog("Download JDK")) {
          comboBox("Version:").dropdown()
          heavyWeightWindow().findText("11").click()
          comboBox("Vendor:").dropdown()
          heavyWeightWindow().findText("AdoptOpenJDK (HotSpot)").click()
          button("Download").click()
        }
        button("OK").click()
      }
    }
  }
}
