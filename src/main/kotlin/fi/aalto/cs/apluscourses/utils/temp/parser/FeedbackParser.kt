package fi.aalto.cs.apluscourses.utils.temp.parser

import fi.aalto.cs.apluscourses.utils.temp.parser.FeedbackParser.TestResults

open class FeedbackParser {
    open fun parseTestResults(htmlString: String): TestResults {
        return TestResults(-1, -1)
    }

    class TestResults(succeeded: Int, failed: Int) {
        val succeeded: Int
        val failed: Int

        init {
            this.succeeded = succeeded
            this.failed = failed
        }
    }
}
