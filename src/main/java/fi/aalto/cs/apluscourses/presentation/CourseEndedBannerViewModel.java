package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.ui.LightColors;
import fi.aalto.cs.apluscourses.BannerViewModel;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.utils.observable.ObservableCachedReadOnlyProperty;
import java.io.IOException;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;

public class CourseEndedBannerViewModel extends BannerViewModel {
  /**
   * Creates a red banner view model and adds an observer to the user of courseProject.
   */
  public CourseEndedBannerViewModel(@NotNull CourseProject courseProject,
                                    @NotNull Notifier notifier) {
    super(new ObservableCachedReadOnlyProperty<>(() -> {
      var course = courseProject.getCourse();
      var authentication = courseProject.getAuthentication();
      if (authentication == null) {
        return null;
      }
      String text = null;
      try {
        var endingTime = course.getExerciseDataSource().getEndingTime(course, authentication);
        if (endingTime.compareTo(ZonedDateTime.now()) < 0) {
          text = getText("ui.BannerView.courseEnded", courseProject.getProject());
        }
      } catch (IOException e) {
        notifier.notify(new NetworkErrorNotification(e), courseProject.getProject());
      }
      return text;
    }), LightColors.RED);
    text.declareDependentOn(courseProject.user);
  }
}
