package fi.aalto.cs.apluscourses.services

object PluginSettings {
    const val REPL_ADDITIONAL_ARGUMENTS_FILE_NAME: String = ".repl-arguments"

    const val MODULE_REPL_INITIAL_COMMANDS_FILE_NAME: String = ".repl-commands"

    const val A_PLUS: String = "A+"

    //  15 minutes in milliseconds
    const val UPDATE_INTERVAL: Long = 15L * 60 * 1000

    //  15 seconds in milliseconds
    const val REASONABLE_DELAY_FOR_MODULE_INSTALLATION: Long = 15L * 1000
}
