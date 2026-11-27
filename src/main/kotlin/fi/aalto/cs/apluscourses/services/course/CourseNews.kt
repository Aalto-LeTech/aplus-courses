package fi.aalto.cs.apluscourses.services.course

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import fi.aalto.cs.apluscourses.model.news.NewsItem
import fi.aalto.cs.apluscourses.notifications.CourseNewsNotification
import fi.aalto.cs.apluscourses.services.Notifier

/** Tracks fetched news, read state and notifications for this project. */
@Service(Service.Level.PROJECT)
class CourseNews(private val project: Project) {
    @Volatile
    private var items: List<NewsItem> = emptyList()

    val current: List<NewsItem> get() = items

    fun hasUnread(): Boolean = items.any { !it.isRead }

    /** Preserves items marked read during this session when applying a refresh. */
    @Synchronized
    fun refreshed(fetched: List<NewsItem>) {
        // A refresh may have started before the user marked these items read.
        val readHere = items.filter { it.isRead }.mapTo(mutableSetOf()) { it.id }
        val carried = fetched.map { if (it.id in readHere) it.copy(isRead = true) else it }
        items = carried
        notifyOf(carried)
        fireUpdated()
    }

    @Synchronized
    fun setAllRead() {
        val read = items.takeIf { it.isNotEmpty() } ?: return
        items = read.map { it.copy(isRead = true) }
        CourseFileManager.getInstance(project).setNewsRead()
        fireUpdated()
    }

    /** Clears news when authentication or course selection changes. */
    @Synchronized
    fun clear() {
        items = emptyList()
    }

    private fun notifyOf(news: List<NewsItem>) {
        val fresh = newsWorthAnnouncing(news, announcedIds)
        announcedIds.addAll(news.map { it.id })
        val latest = fresh.maxByOrNull { it.publish?.toInstant() ?: java.time.Instant.MIN } ?: return
        // The count includes previously announced items that remain unread.
        Notifier.notify(CourseNewsNotification(latest, news.count { !it.isRead }), project)
    }

    private fun fireUpdated() {
        application.invokeLater {
            if (project.isDisposed) return@invokeLater
            project.messageBus.syncPublisher(TOPIC).onNewsUpdated()
        }
    }

    private val announcedIds = mutableSetOf<Long>()

    interface Listener {
        /** Notifies listeners on the EDT after the news state changes. */
        @RequiresEdt
        fun onNewsUpdated()
    }

    companion object {
        @ProjectLevel
        val TOPIC: Topic<Listener> = Topic(Listener::class.java, Topic.BroadcastDirection.TO_CHILDREN)

        fun getInstance(project: Project): CourseNews = project.service()
    }
}

internal fun newsWorthAnnouncing(news: List<NewsItem>, announced: Set<Long>): List<NewsItem> =
    news.filter { !it.isRead && it.id !in announced }
