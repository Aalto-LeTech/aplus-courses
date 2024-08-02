package fi.aalto.cs.apluscourses.model.component.old

abstract class OldLibrary protected constructor(name: String) : OldComponent(name) {
    override fun computeDependencies(): List<String> {
        return emptyList()
    }

    override val isUpdatable: Boolean
        get() = false

    override fun hasLocalChanges(): Boolean {
        return false
    }
}
