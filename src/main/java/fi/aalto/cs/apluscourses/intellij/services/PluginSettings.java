package fi.aalto.cs.apluscourses.intellij.services;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginSettings implements MainViewModelProvider {

  public static final String COURSE_CONFIGURATION_FILE_PATH = "o1.json";

  @NotNull
  private final ConcurrentMap<Project, MainViewModel> mainViewModels = new ConcurrentHashMap<>();

  @NotNull
  public static PluginSettings getInstance() {
    return ServiceManager.getService(PluginSettings.class);
  }

  @NotNull
  public MainViewModel getMainViewModel(@Nullable Project project) {
    return mainViewModels.computeIfAbsent(project, p -> new MainViewModel());
  }
}
