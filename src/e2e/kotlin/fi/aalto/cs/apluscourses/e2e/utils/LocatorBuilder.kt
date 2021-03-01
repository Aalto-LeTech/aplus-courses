package fi.aalto.cs.apluscourses.e2e.utils

import com.intellij.remoterobot.search.locators.byXpath

class LocatorBuilder {
  private val clauses = mutableListOf<String>()

  fun withClause(clause: String): LocatorBuilder {
    clauses.add("($clause)")
    return this
  }

  fun withClass(className: String) =
    withClause("@javaclass='$className' or contains(@classhierarchy, '$className')")

  fun <T> withClass(clazz: Class<T>) = withClass(clazz.name)

  fun withAttr(attr: String, value: String) = withClause("@$attr='$value'")

  private fun toXpath() = clauses.joinToString(" and ", "//div[", "]")

  fun build() = byXpath(toXpath())
}
