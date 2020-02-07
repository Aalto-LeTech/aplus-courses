package fi.aalto.cs.apluscourses.intellij.services;

import com.intellij.openapi.components.ServiceManager;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import org.jetbrains.annotations.NotNull;

public class PluginSettings {

  public static final String COURSE_CONFIGURATION_FILE_PATH = "o1.json";

  @NotNull
  private final MainViewModel mainViewModel = new MainViewModel();

  @NotNull
  public static PluginSettings getInstance() {
    return ServiceManager.getService(PluginSettings.class);
  }

  @NotNull
  public MainViewModel getMainViewModel() {
    return mainViewModel;
  }
}
