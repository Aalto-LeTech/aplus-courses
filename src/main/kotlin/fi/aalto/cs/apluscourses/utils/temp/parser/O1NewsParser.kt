package fi.aalto.cs.apluscourses.utils.temp.parser

import org.jetbrains.annotations.NonNls
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class O1NewsParser(private val language: String) : NewsParser() {

    override fun parseTitle(titleElement: Element): String {
        return getElementsByLanguage(titleElement).first().text()
    }

    override fun parseBody(bodyElement: Element): String {
        return getElementsByLanguage(bodyElement).html()
    }

    private fun getElementsByLanguage(element: Element): Elements {
        @NonNls val className = "only$language"
        @NonNls val defaultClassName = "onlyen"
        val elements = element.getElementsByClass(className)
        if (elements.isEmpty) {
            return element.getElementsByClass(defaultClassName)
        }
        return elements
    }

    companion object {
        const val NAME: String = "O1"
    }
}
