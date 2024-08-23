package fi.aalto.cs.apluscourses.utils.parser

import fi.aalto.cs.apluscourses.utils.parser.FeedbackParser.TestResults
import org.jsoup.Jsoup
import java.util.regex.Pattern

class S2FeedbackParser : FeedbackParser() {
    /**
     * Parses TestResults for S2 from an HTML string.
     */
    override fun parseTestResults(htmlString: String): TestResults {
        val body = Jsoup.parseBodyFragment(htmlString).body()
        val lastP = body.select("p").last()
        if (lastP == null) {
            return TestResults(-1, -1)
        }
        val results = lastP.text()
        val pattern = Pattern.compile("success:\\s+(\\d+),\\s+failed:\\s+(\\d+)")
        val matcher = pattern.matcher(results)
        if (!matcher.find()) {
            return TestResults(-1, -1)
        }
        return TestResults(matcher.group(1).toInt(), matcher.group(2).toInt())
    }

    companion object {
        const val NAME: String = "S2"
    }
}
