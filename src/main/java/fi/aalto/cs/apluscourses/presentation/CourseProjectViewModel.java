package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadOnlyProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectViewModel {

  public final ObservableProperty<Boolean> restartProperty;
  public final ObservableProperty<Boolean> settingsOptOutProperty;
  public final ObservableProperty<Boolean> isRestartAvailableProperty;

  @NotNull
  private final Course course;
  @Nullable
  private final String currentlyImportedIdeSettings;
  private final boolean currentSettingsDiffer;

  /**
   * Construct a course project view model with the given course and name of the currently imported
   * IDE settings.
   * @param course                       The course bound to this course project view model.
   * @param currentlyImportedIdeSettings The ID of the course for which the IDE settings have
   *                                     currently been imported.
   */
  public CourseProjectViewModel(@NotNull Course course,
                                @Nullable String currentlyImportedIdeSettings) {
    this.course = course;
    this.currentlyImportedIdeSettings = currentlyImportedIdeSettings;
    currentSettingsDiffer = !course.getId().equals(currentlyImportedIdeSettings);

    restartProperty = new ObservableReadWriteProperty<>(currentSettingsDiffer);

    settingsOptOutProperty = new ObservableReadWriteProperty<>(!currentSettingsDiffer);

    isRestartAvailableProperty = new ObservableReadOnlyProperty<>(this::isRestartAvailable);
    isRestartAvailableProperty.declareDependentOn(settingsOptOutProperty);
    isRestartAvailableProperty.addValueObserver(this, CourseProjectViewModel::onRestartEnabled);
  }

  private void onRestartEnabled(boolean restartEnabled) {
    if (!restartEnabled) {
      restartProperty.set(false);
    }
  }

  public boolean shouldWarnUser() {
    return currentSettingsDiffer;
  }

  public boolean shouldShowSettingsInfo() {
    return currentSettingsDiffer;
  }

  public boolean canUserOptOutSettings() {
    return currentSettingsDiffer;
  }

  public boolean isRestartAvailable() {
    return currentSettingsDiffer && !userOptsOutOfSettings();
  }

  public boolean userWantsRestart() {
    return Boolean.TRUE.equals(restartProperty.get());
  }

  public boolean userOptsOutOfSettings() {
    return Boolean.TRUE.equals(settingsOptOutProperty.get());
  }

  public String getCourseName() {
    return course.getName();
  }

  public boolean shouldShowCurrentSettings() {
    return !currentSettingsDiffer;
  }
}
