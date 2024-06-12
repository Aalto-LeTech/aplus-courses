package fi.aalto.cs.apluscourses.model.news

class NewsTree(val news: List<NewsItem> = emptyList()) {
    fun setAllRead() {
        news.forEach { newsItem: NewsItem -> newsItem.setRead() }
    }

    fun unreadCount(): Int {
        return news.count { n: NewsItem -> !n.isRead }
    }
}
