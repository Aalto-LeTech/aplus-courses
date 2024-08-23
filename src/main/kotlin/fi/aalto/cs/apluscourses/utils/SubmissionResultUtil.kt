package fi.aalto.cs.apluscourses.utils

import fi.aalto.cs.apluscourses.MyBundle.message
import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult

object SubmissionResultUtil {
    /**
     * Creates a status text for a submission result.
     */
    fun getStatus(submissionResult: SubmissionResult): String {
        val testsFailed = submissionResult.testsFailed
        val testsFailedResourceKey =
            if (testsFailed == 1) "presentation.submissionResultViewModel.testFailed" else "presentation.submissionResultViewModel.testsFailed"
        val testsFailedString = if (testsFailed < 1) "" else message(testsFailedResourceKey, testsFailed)
        return message(
            "presentation.submissionResultViewModel.points",
            submissionResult.userPoints, submissionResult.maxPoints
        ) + testsFailedString
    }
}
