package fi.aalto.cs.apluscourses.model.component

import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SbtModuleDependenciesTest {

    @Test
    fun `a module's own sbt submodules are not dependencies`() {
        @NonNls val dependencies = setOf(
            "$SBT_MODULE.main",
            "$SBT_MODULE.test",
            "$SBT_MODULE.$SBT_MODULE-build"
        )

        assertTrue(
            SbtModule.withoutOwnSubmodules(dependencies, SBT_MODULE).isEmpty(),
            "a module's own parts are not dependencies"
        )
    }

    @Test
    fun `dependencies on other modules and libraries are kept`() {
        val dependencies = setOf(LIBRARY_MODULE, SCALA_SDK)

        assertEquals(dependencies, SbtModule.withoutOwnSubmodules(dependencies, OTHER_MODULE))
    }

    @Test
    fun `a module whose name merely starts with this one's is kept`() {
        @NonNls val dependencies = setOf("${PREFIX_MODULE}Extras.main")

        assertEquals(dependencies, SbtModule.withoutOwnSubmodules(dependencies, PREFIX_MODULE))
    }

    private companion object {
        @NonNls
        const val SBT_MODULE = "a1120-scala3-r01-warmup"

        @NonNls
        const val LIBRARY_MODULE = "O1Library"

        @NonNls
        const val SCALA_SDK = "scala-sdk-3.3.1"

        @NonNls
        const val OTHER_MODULE = "OneWeek"

        @NonNls
        const val PREFIX_MODULE = "warmup"
    }
}
