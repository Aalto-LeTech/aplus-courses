package fi.aalto.cs.apluscourses.e2e.utils

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO

object StepLoggerInitializer {
  private val initialized = AtomicBoolean(false)
  fun init() {
    if (!initialized.getAndSet(true)) {
      StepWorker.registerProcessor(StepLogger())
    }
  }
}

fun uiTest(test: RemoteRobot.() -> Unit) {
  val remoteRobot = RemoteRobot("http://localhost:8082")
  try {
    remoteRobot.test()
  } catch (e: Throwable) {
    remoteRobot.fetchScreenShot().save("error")
    throw e
  }
}


private fun BufferedImage.save(name: String) {
  val bytes = ByteArrayOutputStream().use { b ->
    ImageIO.write(this, "png", b)
    b.toByteArray()
  }
  File("build/hierarchy-reports").apply { mkdirs() }.resolve("$name.png").writeBytes(bytes)
}

private fun RemoteRobot.fetchScreenShot(): BufferedImage {
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

fun wait(duration: Duration) = Thread.sleep(duration.toMillis())
