package fi.aalto.cs.apluscourses.utils.parser

import org.jsoup.nodes.Element

open class NewsParser {
    open fun parseTitle(titleElement: Element): String {
        return titleElement.text()
    }

    open fun parseBody(bodyElement: Element): String {
        return bodyElement.html()
    }
}
