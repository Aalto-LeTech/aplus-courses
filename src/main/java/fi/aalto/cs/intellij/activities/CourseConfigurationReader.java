package fi.aalto.cs.intellij.activities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.io.CourseInformation;
import org.jetbrains.annotations.NotNull;

public class CourseConfigurationReader implements StartupActivity {
  @Override
  public void runActivity(@NotNull Project project) {
    CourseInformation.parse("o1.json");
  }
}
