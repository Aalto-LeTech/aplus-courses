package fi.aalto.cs.apluscourses.e2e.utils

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import java.io.File
import java.io.FileInputStream
import java.time.Duration
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

object StepLoggerInitializer {
    private val initialized = AtomicBoolean(false)
    fun init() {
        if (!initialized.getAndSet(true)) {
            StepWorker.registerProcessor(StepLogger())
        }
    }
}

fun uiTest(test: RemoteRobot.() -> Unit) {
    RemoteRobot("http://localhost:8082").test()
}

fun wait(duration: Duration) = Thread.sleep(duration.toMillis())

fun getVersion(): String {
    val versionProperties = Properties()
    val projectDir = File("").absolutePath
    versionProperties.load(FileInputStream("$projectDir/build/resources/main/build-info.properties"))
    return versionProperties.getProperty("version")
}
