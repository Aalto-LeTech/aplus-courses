package fi.aalto.cs.apluscourses.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Reads a field that A+ does not always send as a boolean.
 *
 * `has_submittable_files` comes back as `false`, as `null`, or as an empty array.
 */
object TruthyBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TruthyBoolean", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeBoolean()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.booleanOrNull ?: false
            is JsonArray -> element.isNotEmpty()
            else -> false
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean): Unit = encoder.encodeBoolean(value)
}
