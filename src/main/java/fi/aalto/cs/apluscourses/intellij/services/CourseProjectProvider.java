package fi.aalto.cs.apluscourses.intellij.services;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import org.jetbrains.annotations.Nullable;

public interface CourseProjectProvider {

  @Nullable CourseProject getCourseProject(@Nullable Project project);

}
