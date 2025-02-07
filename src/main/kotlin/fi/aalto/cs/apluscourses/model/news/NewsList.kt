package fi.aalto.cs.apluscourses.model.news

data class NewsList(val news: List<NewsItem> = emptyList()) {
    fun setAllRead() {
        news.forEach { it.isRead = true }
    }

    fun setRead(id: Long) {
        news.find { it.id == id }?.isRead = true
    }

    fun unreadCount(): Int {
        return news.count { !it.isRead }
    }
}
