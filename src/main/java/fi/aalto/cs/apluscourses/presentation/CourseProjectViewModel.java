package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.utils.ObservableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectViewModel {

  private final String informationText;
  private final String settingsText;

  private final ObservableProperty.ValueObserver<Boolean> settingsOptOutObserver;
  private final ObservableProperty.ValueObserver<Boolean> cancelObserver;

  public final ObservableProperty<Boolean> cancel;
  public final ObservableProperty<Boolean> restart;
  public final ObservableProperty<Boolean> settingsOptOut;
  public final ObservableProperty<Boolean> isRestartAvailable;
  public final ObservableProperty<Boolean> isSettingsOptOutAvailable;

  /**
   * Construct a course project view model with the given course and name of the currently imported
   * IDE settings.
   * @param course                       The course bound to this course project view model.
   * @param currentlyImportedIdeSettings The name of the course for which the IDE settings have
   *                                     currently been imported.
   */
  public CourseProjectViewModel(@NotNull Course course,
                                @Nullable String currentlyImportedIdeSettings) {
    cancel = new ObservableProperty<>(false);

    String courseName = course.getName();
    informationText = "<html><body>The currently opened project will be turned into a project for "
        + "the course <b>" + courseName + "</b>.</body></html>";

    if (courseName.equals(currentlyImportedIdeSettings)) {
      settingsText = "<html><body>IntelliJ IDEA settings are already imported for <b>" + courseName
          + "</b>.</body><html>";
      restart = new ObservableProperty<>(false);
      settingsOptOut = new ObservableProperty<>(true);
      isRestartAvailable = new ObservableProperty<>(false);
      isSettingsOptOutAvailable = new ObservableProperty<>(false);
    } else {
      settingsText = "<html><body>The A+ Courses plugin will adjust IntelliJ IDEA settings. This "
          + "helps use IDEA for coursework.</body></html>";
      restart = new ObservableProperty<>(true);
      settingsOptOut = new ObservableProperty<>(false);
      isRestartAvailable = new ObservableProperty<>(true);
      isSettingsOptOutAvailable = new ObservableProperty<>(true);
    }

    settingsOptOutObserver = optOut -> {
      isRestartAvailable.set(!optOut);
      if (optOut) {
        restart.set(false);
      }
    };
    settingsOptOut.addValueObserver(settingsOptOutObserver);

    cancelObserver = cancel -> {
      if (cancel) {
        restart.set(false);
        settingsOptOut.set(true);
      }
    };
    cancel.addValueObserver(cancelObserver);
  }

  @NotNull
  public String getInformationText() {
    return informationText;
  }

  @NotNull
  public String getSettingsText() {
    return settingsText;
  }

  /** Returns the text that should be displayed next to the restart checkbox. */
  @NotNull
  public String getRestartCheckboxText() {
    return "Restart IntelliJ to reload settings.";
  }

  /** Returns the text that should be displayed next to the settings opt out checkbox. */
  @NotNull
  public String getOptOutCheckboxText() {
    if (isSettingsOptOutAvailable.get()) {
      return "<html><body>Leave IntelliJ settings unchanged.<br>(<b>Not recommended</b>. Only pick "
          + "this option if you are sure you know what you are doing.)</body></html>";
    } else {
      return "Leave IntelliJ settings unchanged.";
    }
  }

  public boolean userCancels() {
    return Boolean.TRUE.equals(cancel.get());
  }

  public boolean userWantsRestart() {
    return Boolean.TRUE.equals(restart.get());
  }

  public boolean userOptsOutOfSettings() {
    return Boolean.TRUE.equals(settingsOptOut.get());
  }
}
