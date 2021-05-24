package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.ui.LightColors;
import fi.aalto.cs.apluscourses.BannerViewModel;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import java.io.IOException;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseEndedBannerViewModel extends BannerViewModel {
  private static final Logger logger = LoggerFactory.getLogger(CourseEndedBannerViewModel.class);

  @NotNull
  private final CourseProject courseProject;

  /**
   * Creates a red banner view model and adds an observer to the user of courseProject.
   */
  public CourseEndedBannerViewModel(@NotNull CourseProject courseProject) {
    super(LightColors.RED);
    this.courseProject = courseProject;
    courseProject.user.addSimpleObserver(this, CourseEndedBannerViewModel::update);
  }

  /**
   * Fetches the course ending time and if it has passed, updates the text.
   */
  @Override
  public void update() {
    var course = courseProject.getCourse();
    var authentication = courseProject.getAuthentication();
    if (authentication == null) {
      return;
    }
    try {
      var endingTime = course.getExerciseDataSource().getEndingTime(course, authentication);
      if (endingTime.compareTo(ZonedDateTime.now()) < 0) {
        this.text.set(getText("ui.BannerView.courseEnded"));
      }
    } catch (IOException e) {
      logger.error("Failed to fetch ending time", e);
    }
  }
}
