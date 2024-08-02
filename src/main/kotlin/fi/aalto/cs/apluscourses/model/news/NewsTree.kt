package fi.aalto.cs.apluscourses.model.news

class NewsTree(val news: List<NewsItem> = emptyList()) {
    fun setAllRead() {
        news.forEach { newsItem: NewsItem -> newsItem.isRead = true }
    }

    fun setRead(id: Long) {
        news.find { n: NewsItem -> n.id == id }?.isRead = true
    }

    fun unreadCount(): Int {
        return news.count { n: NewsItem -> !n.isRead }
    }
}
