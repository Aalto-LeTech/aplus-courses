package fi.aalto.cs.apluscourses.e2e.utils

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker

object StepLoggerInitializer {
  private var initialized = false;
  fun init() = synchronized(initialized) {
    if (!initialized) {
      StepWorker.registerProcessor(StepLogger())
      initialized = true
    }
  }
}

fun uiTest(test: RemoteRobot.() -> Unit) {
  RemoteRobot("http://127.0.0.1:6942").test()
}
