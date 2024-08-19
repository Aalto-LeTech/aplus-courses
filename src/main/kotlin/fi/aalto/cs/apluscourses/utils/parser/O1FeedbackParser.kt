package fi.aalto.cs.apluscourses.utils.parser

import org.jsoup.Jsoup
import java.util.regex.Pattern

class O1FeedbackParser : FeedbackParser() {
    /**
     * Parses TestResults for O1 from an HTML string.
     */
    override fun parseTestResults(htmlString: String): TestResults {
        val body = Jsoup.parseBodyFragment(htmlString).body()
        val lastH3 = body.getElementsByTag("h3").last()
        if (lastH3 == null || lastH3.nextElementSibling() == null) {
            return TestResults(-1, -1)
        }
        val results = lastH3.nextElementSibling()?.text()
        val pattern = Pattern.compile("(\\d+)\\s+succeeded,\\s+(\\d+)\\s+failed,\\s+(\\d+)\\s+canceled")
        val matcher = pattern.matcher(results)
        if (!matcher.find()) {
            return TestResults(-1, -1)
        }
        return TestResults(
            matcher.group(1).toInt(),
            matcher.group(2).toInt() + matcher.group(3).toInt()
        )
    }

    companion object {
        const val NAME: String = "O1"
    }
}
