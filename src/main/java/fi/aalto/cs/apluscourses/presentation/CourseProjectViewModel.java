package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Course;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectViewModel {

  String informationText;
  String settingsText;

  private boolean cancel;
  private boolean optOut;
  private boolean restart;
  private boolean restartAvailable;
  private boolean optOutAvailable;

  /**
   * Construct a course project view model with the given course and name of the currently imported
   * IDE settings.
   * @param course                       The course bound to this course project view model.
   * @param currentlyImportedIdeSettings The name of the course for which the IDE settings have
   *                                     currently been imported.
   */
  public CourseProjectViewModel(@NotNull Course course,
                                @Nullable String currentlyImportedIdeSettings) {
    cancel = false;

    String courseName = course.getName();
    informationText = "<html><body>The currently opened project will be turned into a project for "
        + "the course <b>" + courseName + "</b>.</body></html>";

    if (courseName.equals(currentlyImportedIdeSettings)) {
      settingsText = "<html><body>IntelliJ IDEA settings are already imported for <b>" + courseName
          + "</b>.</body><html>";
      restart = false;
      optOut = true;
      restartAvailable = false;
      optOutAvailable = false;
    } else {
      settingsText = "<html><body>The A+ Courses plugin will adjust IntelliJ IDEA settings. This "
          + "helps use IDEA for coursework.</body></html>";
      restart = true;
      optOut = false;
      restartAvailable = true;
      optOutAvailable = true;
    }
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
    if (optOutAvailable) {
      return "<html><body>Leave IntelliJ settings unchanged.<br>(<b>Not recommended</b>. Only pick "
          + "this option if you are sure you know what you are doing.)</body></html>";
    } else {
      return "Leave IntelliJ settings unchanged.";
    }
  }

  @NotNull
  public boolean userCancels() {
    return cancel;
  }

  public boolean userWantsSettings() {
    return !optOut;
  }

  public boolean userWantsRestart() {
    return restart;
  }

  /**
   * Updates the state of this view model to reflect that the user cancelled the course project
   * action.
   */
  public void cancel() {
    cancel = true;
    restart = false;
    optOut = true;
  }

  public void setRestart(boolean userWantsRestart) {
    restart = userWantsRestart;
  }

  /**
   * Sets the settings opt out to the given value and updates the model to reflect the change.
   */
  public void setSettingsOptOut(boolean userOptsOut) {
    this.optOut = userOptsOut;
    this.restartAvailable = !userOptsOut;
    if (userOptsOut) {
      this.restart = false;
    }
  }

  public boolean isRestartAvailable() {
    return restartAvailable;
  }

  public boolean isOptOutAvailable() {
    return optOutAvailable;
  }

}
