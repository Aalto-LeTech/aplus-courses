package fi.aalto.cs.apluscourses.model.component.old

import com.intellij.util.concurrency.annotations.RequiresReadLock

interface IntelliJComponent<T> {
    @get:RequiresReadLock
    val platformObject: T?
}
