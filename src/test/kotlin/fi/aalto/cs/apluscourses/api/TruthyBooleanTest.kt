package fi.aalto.cs.apluscourses.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.Test

class TruthyBooleanTest {
    @Serializable
    private data class Holder(
        @Serializable(with = TruthyBooleanSerializer::class)
        val hasSubmittableFiles: Boolean = false
    )

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    private fun decode(@NonNls body: String) = json.decodeFromString<Holder>(body).hasSubmittableFiles

    @Test
    fun `a plain boolean is read as itself`() {
        assertTrue(decode("""{"has_submittable_files":true}"""))
        assertFalse(decode("""{"has_submittable_files":false}"""))
    }

    @Test
    fun `an empty array means there are no submittable files`() {
        assertFalse(decode("""{"has_submittable_files":[]}"""))
    }

    @Test
    fun `an empty array is handled however the response is spaced`() {
        assertFalse(decode("""{"has_submittable_files": []}"""))
        assertFalse(decode("{\"has_submittable_files\":\n  []}"))
    }

    @Test
    fun `a null is read as no files`() {
        assertFalse(decode("""{"has_submittable_files":null}"""))
    }

    @Test
    fun `a missing field falls back to the default`() {
        assertFalse(decode("""{}"""))
    }

    @Test
    fun `a non-empty array means there are files`() {
        assertTrue(decode("""{"has_submittable_files":["a.scala"]}"""))
    }
}
