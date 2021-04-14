package fi.aalto.cs.apluscourses.e2e.utils

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.time.Duration
import java.util.*
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
        HierarchyDownloader.catchHierarchy {
            remoteRobot.fetchScreenShot().save("error_${System.currentTimeMillis()}")
        }
        throw e
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
    return callJs<ByteArray>(
        """
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

fun getVersion(): String {
    val versionProperties = Properties()
    val projectDir = File("").absolutePath
    versionProperties.load(FileInputStream("$projectDir/build/resources/main/build-info.properties"))
    return versionProperties.getProperty("version")
}

object HierarchyDownloader {
    private val client = OkHttpClient()
    private const val baseUrl = "http://127.0.0.1:8082"

    fun catchHierarchy(code: () -> Unit) {
        try {
            code()
        } finally {
            saveHierarchy()
        }
    }

    private fun saveHierarchy() {
        val hierarchySnapshot =
            saveFile(baseUrl, "build/hierarchy-reports", "hierarchy-${System.currentTimeMillis()}.html")
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
            writeText(response.body?.string() ?: "")
        }
    }
}
