package fi.aalto.cs.apluscourses.ui.temp.presentation

import fi.aalto.cs.apluscourses.model.Course

class CourseProjectViewModel(
    course: Course,
    currentlyImportedIdeSettings: Long?
) {
    //
    //  public final ObservableProperty<Boolean> settingsOptOutProperty;
    //
    //  public final ObservableProperty<String> languageProperty
    //      = new ObservableReadWriteProperty<>(null, CourseProjectViewModel::validateLanguage);
    private val course: Course

    private val currentSettingsDiffer: Boolean


    /**
     * Construct a course project view model with the given course and name of the currently imported
     * IDE settings.
     *
     * @param course                       The course bound to this course project view model.
     * @param currentlyImportedIdeSettings The ID of the course for which the IDE settings have
     * currently been imported.
     */
    init {
        this.course = course

        currentSettingsDiffer = currentlyImportedIdeSettings == null ||
                course.id != (currentlyImportedIdeSettings)

        //    settingsOptOutProperty = new ObservableReadWriteProperty<>(!currentSettingsDiffer);
    }

    fun shouldWarnUser(): Boolean {
        return currentSettingsDiffer
    }

    fun shouldShowSettingsInfo(): Boolean {
        return currentSettingsDiffer
    }

    fun canUserOptOutSettings(): Boolean {
        return currentSettingsDiffer
    }

    fun shouldShowSettingsSegment(): Boolean {
        return course.appropriateIdeSettingsUrl != null || !course.vmOptions.isEmpty()
    }

    //  public boolean userOptsOutOfSettings() {
    //    return Boolean.TRUE.equals(settingsOptOutProperty.get());
    //  }
    //  public boolean shouldApplyNewIdeSettings() {
    //    return shouldShowSettingsSegment() && !userOptsOutOfSettings();
    //  }
    fun getCourseName(): String {
        return course.name
    }

    fun getLanguages(): Array<String?> {
        return course.languages.toTypedArray<String?>()
    }

    fun shouldShowCurrentSettings(): Boolean {
        return !currentSettingsDiffer
    }

    fun shouldDisplayVersionWarning(): Boolean {
//    return BuildInfo.INSTANCE.courseVersion.comparisonStatus(course.getMinimumPluginVersion())
//        == Version.ComparisonStatus.MINOR_TOO_OLD;
        return false
    } //  private static ValidationError validateLanguage(@Nullable String language) {
    //    return language == null ? new LanguageNotSelectedError() : null;
    //  }
    //  private static class LanguageNotSelectedError implements ValidationError {
    //    @Override
    //    public @NotNull String getDescription() {
    //      return getText("ui.courseProjectViewModel.languageNotSelected");
    //    }
    //  }
}
