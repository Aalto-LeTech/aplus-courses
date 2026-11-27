package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.text.StringUtil
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.actions.ShowNewsNotificationAction
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.services.PluginSettings

/** Shows the latest unread news item and the number of other unread items. */
class CourseNewsNotification(latest: NewsItem, unreadCount: Int) : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.CourseNewsNotification.title"),
    MyBundle.message(
        "notification.CourseNewsNotification.content",
        StringUtil.escapeXmlEntities(latest.title),
        StringUtil.escapeXmlEntities(excerptOf(latest.body))
    ) + andMore(unreadCount - 1),
    NotificationType.INFORMATION
) {
    init {
        addAction(ShowNewsNotificationAction())
    }
}

private fun andMore(others: Int): String =
    if (others > 0) MyBundle.message("notification.CourseNewsNotification.andMore", others) else ""

/** Removes HTML, collapses whitespace and truncates at a word boundary. */
internal fun excerptOf(body: String, limit: Int = EXCERPT_LENGTH): String {
    val text = StringUtil.unescapeXmlEntities(StringUtil.stripHtml(body, " "))
        .replace(WHITESPACE, " ")
        .trim()
    if (text.length <= limit) return text
    val cut = text.take(limit)
    val lastWordEnd = cut.lastIndexOf(' ')
    val kept = if (lastWordEnd > limit / 2) cut.take(lastWordEnd) else cut
    return kept.trimEnd { it == ' ' || it == ',' || it == '.' || it == ';' || it == ':' } + "…"
}

private val WHITESPACE = Regex("\\s+")

private const val EXCERPT_LENGTH = 120
