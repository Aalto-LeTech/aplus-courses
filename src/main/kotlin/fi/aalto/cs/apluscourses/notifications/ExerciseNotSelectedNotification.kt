package fi.aalto.cs.apluscourses.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import fi.aalto.cs.apluscourses.MyBundle
import fi.aalto.cs.apluscourses.services.PluginSettings

class ExerciseNotSelectedNotification
/**
 * Constructs a notification that notifies the user that no exercise is selected. This should be
 * shown when the user uses the exercise submission button, but no exercise is selected.
 */
    : Notification(
    PluginSettings.A_PLUS,
    MyBundle.message("notification.ExerciseNotSelectedNotification.title"),
    MyBundle.message("notification.ExerciseNotSelectedNotification.content"),
    NotificationType.INFORMATION
)
