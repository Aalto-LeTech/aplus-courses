package fi.aalto.cs.apluscourses.e2e.steps

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.Fixture
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.attempt
import com.intellij.remoterobot.utils.waitFor
import fi.aalto.cs.apluscourses.e2e.fixtures.customComboBox
import fi.aalto.cs.apluscourses.e2e.fixtures.dialog
import fi.aalto.cs.apluscourses.e2e.fixtures.heavyWeightWindow
import fi.aalto.cs.apluscourses.e2e.fixtures.welcomeFrame
import fi.aalto.cs.apluscourses.e2e.utils.fetchScreenShot
import fi.aalto.cs.apluscourses.e2e.utils.save
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import javax.imageio.ImageIO

class CommonSteps(val remoteRobot: RemoteRobot) {

    fun createProject() {
        with(remoteRobot) {
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
                        remoteRobot.fetchScreenShot().save("beforeClick")
                        attempt(2) {
                            findText("Add SDK").click()
                        }
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