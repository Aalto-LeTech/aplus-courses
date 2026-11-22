package fi.aalto.cs.apluscourses

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
const val BUNDLE: String = "messages.resources"

object MyBundle {
    private val bundle = DynamicBundle(MyBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String =
        bundle.getMessage(key, *params)
}
