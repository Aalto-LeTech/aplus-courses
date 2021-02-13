package fi.aalto.cs.apluscourses.e2e.utils

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import java.time.Duration

object StepLoggerInitializer {
  private var initialized = false
  fun init() = synchronized(initialized) {
    if (!initialized) {
      StepWorker.registerProcessor(StepLogger())
      initialized = true
    }
  }
}

fun uiTest(test: RemoteRobot.() -> Unit) {
  RemoteRobot("http://0.0.0.0:8580").test()
//  RemoteRobot("http://localhost:8580").test()
}

fun wait(duration: Duration) = Thread.sleep(duration.toMillis())
