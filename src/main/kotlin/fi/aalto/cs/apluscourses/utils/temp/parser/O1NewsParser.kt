package fi.aalto.cs.apluscourses.utils.temp.parser

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class O1NewsParser(language: String) : NewsParser() {
    private val language: String

    init {
        this.language = language
    }

    override fun parseTitle(titleElement: Element): String {
        return getElementsByLanguage(titleElement).first().text()
    }

    override fun parseBody(bodyElement: Element): String {
        return getElementsByLanguage(bodyElement).html()
    }

    private fun getElementsByLanguage(element: Element): Elements {
        val elements = element.getElementsByClass("only" + language)
        if (elements.isEmpty()) {
            return element.getElementsByClass("onlyen")
        }
        return elements
    }

    companion object {
        const val NAME: String = "O1"
    }
}
