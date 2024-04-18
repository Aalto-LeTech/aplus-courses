package fi.aalto.cs.apluscourses.utils

//import org.json.JSONArray
import java.util.regex.Pattern

class CourseHiddenElements @JvmOverloads constructor(
    hiddenIDs: List<Long>? = null,
    hiddenRegexes: List<Pattern>? = null,
    hiddenLanguageSpecificRegexes: Map<String, MutableList<Pattern>>? = null
) {
    private val hiddenIDs: MutableList<Long> = ArrayList()

    private val hiddenRegexes: MutableList<Pattern> = ArrayList()

    private val hiddenLanguageSpecificRegexes: MutableMap<String, MutableList<Pattern>> = HashMap()

    /**
     * Checks if the object (e.g. exercise) ID or string match at least one of hiding rules.
     *
     * @return True of the object is supposed to be hidden, false otherwise.
     */
    fun shouldHideObject(objectId: Long, objectName: String, currentLanguage: String?): Boolean {
        if (hiddenIDs.contains(objectId)) {
            return true
        }

        if (hiddenRegexes.stream().anyMatch { r: Pattern -> r.matcher(objectName).find() }) {
            return true
        }

        return currentLanguage != null && hiddenLanguageSpecificRegexes.getOrDefault(currentLanguage, ArrayList())
            .stream().anyMatch { r: Pattern -> r.matcher(objectName).find() }
    }

    /**
     * Constructor.
     */
    /**
     * Constructor without any hiding rules.
     */
    init {
        if (hiddenIDs != null) {
            this.hiddenIDs.addAll(hiddenIDs)
        }

        if (hiddenRegexes != null) {
            this.hiddenRegexes.addAll(hiddenRegexes)
        }

        if (hiddenLanguageSpecificRegexes != null) {
            this.hiddenLanguageSpecificRegexes.putAll(hiddenLanguageSpecificRegexes)
        }
    }

//    companion object {
//        /**
//         * Constructs the CourseHiddenElements object from an array of JSON objects.
//         * Each JSON object must either have a "byId" integer-valued element (in order to
//         * hide the element by its ID), a "byRegex" string-valued element (to hide the element
//         * by a case-insensitive regex match), or a "byRegex" object-valued element (same as string,
//         * but language-specific).
//         */
//        fun fromJsonObject(hiddenElementsArray: JSONArray): CourseHiddenElements {
//            val hiddenIDs: MutableList<Long> = ArrayList()
//            val hiddenRegexes: MutableList<Pattern> = ArrayList()
//            val hiddenLanguageSpecificRegexes: MutableMap<String, MutableList<Pattern>> = HashMap()
//
//            for (i in 0 until hiddenElementsArray.length()) {
//                val hiddenObject: Any = hiddenElementsArray.get(i)
//
//                if (hiddenObject is Int) {
//                    hiddenIDs.add(hiddenObject.toLong()) // hiding by object ID
//                } else if (hiddenObject is String) {
//                    hiddenRegexes.add(Pattern.compile(hiddenObject, Pattern.CASE_INSENSITIVE)) // hiding by regex
//                } else if (hiddenObject is JSONObject) {
//                    for (key in hiddenObject.keySet()) { // hiding by language-specific regex
//                        val regex: String = hiddenObject.getString(key)
//                        hiddenLanguageSpecificRegexes.computeIfAbsent(key) { k: String? -> ArrayList() }
//                            .add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE))
//                    }
//                } else {
//                    throw JSONException("Element number $i in the \"hiddenElements\" array is of an unsupported type")
//                }
//            }
//
//            return CourseHiddenElements(hiddenIDs, hiddenRegexes, hiddenLanguageSpecificRegexes)
//        }
//    }
}
