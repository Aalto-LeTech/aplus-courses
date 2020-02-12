package fi.aalto.cs.apluscourses.intellij.services;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MainViewModelProvider {
  @NotNull
  MainViewModel getMainViewModel(@Nullable Project project);
}
