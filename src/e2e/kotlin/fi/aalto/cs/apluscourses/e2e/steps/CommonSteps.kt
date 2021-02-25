package fi.aalto.cs.apluscourses.e2e.steps

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.Fixture
import com.intellij.remoterobot.stepsProcessing.step
import fi.aalto.cs.apluscourses.e2e.fixtures.customComboBox
import fi.aalto.cs.apluscourses.e2e.fixtures.dialog
import fi.aalto.cs.apluscourses.e2e.fixtures.heavyWeightWindow
import fi.aalto.cs.apluscourses.e2e.fixtures.welcomeFrame
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import javax.imageio.ImageIO

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
            HierarchyDownloader.catchHierarchy {
              try {
                heavyWeightWindow().findText("Download JDK...").click()
              } catch (e: Throwable) {
                remoteRobot.fetchScreenShot().save("error")
              }
            }
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

fun BufferedImage.save(name: String) {
  val bytes = ByteArrayOutputStream().use { b ->
    ImageIO.write(this, "png", b)
    b.toByteArray()
  }
  File("build/hierarchy-reports").apply { mkdirs() }.resolve("$name.png").writeBytes(bytes)
}

fun RemoteRobot.fetchScreenShot(): BufferedImage {
  return callJs<ByteArray>("""
            importPackage(java.io)
            importPackage(javax.imageio)
            const screenShot = new java.awt.Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            let pictureBytes;
            const baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(screenShot, "png", baos);
                pictureBytes = baos.toByteArray();
            } finally {
              baos.close();
            }
            pictureBytes;
        """
  ).inputStream().use {
    ImageIO.read(it)
  }
}

object HierarchyDownloader {
  private val client = OkHttpClient()
  private const val baseUrl = "http://127.0.0.1:8082"

  fun catchHierarchy(code: () -> Unit) {
    try {
      code()
    } finally {
      HierarchyDownloader.saveHierarchy()
    }
  }

  private fun saveHierarchy() {
    val hierarchySnapshot = saveFile(baseUrl, "build/hierarchy-reports", "hierarchy-${System.currentTimeMillis()}.html")
    if (File("build/hierarchy-reports/styles.css").exists().not()) {
      saveFile("$baseUrl/styles.css", "build/hierarchy-reports", "styles.css")
    }
    println("Hierarchy snapshot: ${hierarchySnapshot.absolutePath}")
  }
  private fun saveFile(url: String, folder: String, name: String): File {
    val response = client.newCall(Request.Builder().url(url).build()).execute()
    return File(folder).apply {
      mkdirs()
    }.resolve(name).apply {
      writeText(response.body()?.string() ?: "")
    }
  }
}