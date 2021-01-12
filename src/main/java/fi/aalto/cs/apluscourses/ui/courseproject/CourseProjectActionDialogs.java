package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseSelectionViewModel;
import org.jetbrains.annotations.NotNull;

public interface CourseProjectActionDialogs {

  boolean showCourseSelectionDialog(@NotNull Project project,
                                    @NotNull CourseSelectionViewModel courseSelectionViewModel);

  boolean showMainDialog(@NotNull Project project,
                         @NotNull CourseProjectViewModel courseProjectViewModel);

  boolean showRestartDialog();

}
