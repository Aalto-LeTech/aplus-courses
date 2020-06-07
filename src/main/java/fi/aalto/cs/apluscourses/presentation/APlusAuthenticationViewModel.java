package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthenticationViewModel {
  Project project;
  APlusAuthentication authentication;

  public APlusAuthenticationViewModel(@Nullable Project project) {
    this.project = project;
    this.authentication = null;
  }

  public void setToken(@NotNull char[] token) {
    authentication = new APlusAuthentication(token);
  }

  @Nullable
  public Project getProject() {
    return project;
  }

  @Nullable
  public APlusAuthentication getAuthentication() {
    return authentication;
  }
}
