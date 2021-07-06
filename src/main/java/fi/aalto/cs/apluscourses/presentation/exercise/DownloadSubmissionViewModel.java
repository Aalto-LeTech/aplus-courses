package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DownloadSubmissionViewModel {
  @NotNull
  private final List<Module> modules;

  @NotNull
  public final ObservableProperty<Module> selectedModule;

  @NotNull
  public final ObservableProperty<String> moduleName;

  @NotNull
  private final List<String> installedModules;

  /**
   * A constructor.
   */
  public DownloadSubmissionViewModel(@NotNull Course course,
                                     @Nullable Module currentSelectedModule,
                                     long submissionId,
                                     @NotNull Project project) {
    this.modules = course.getModules();
    this.installedModules = Arrays
        .stream(ModuleManager.getInstance(project).getModules())
        .map(com.intellij.openapi.module.Module::getName)
        .collect(Collectors.toList());
    this.selectedModule = new ObservableReadWriteProperty<>(currentSelectedModule);
    this.moduleName = new ObservableReadWriteProperty<>(submissionId + "_"
        + (currentSelectedModule == null ? "" : currentSelectedModule.getName()));
    selectedModule.addValueObserver(selectedModule, (m, dummy) -> {
      var module = m.get();
      moduleName.set(submissionId + "_"
          + (module == null ? "" : module.getName()));
    });
  }

  @NotNull
  public List<Module> getModules() {
    return modules;
  }

  public String getPrompt() {
    return getText("presentation.moduleSelectionViewModel.prompt");
  }

  @NotNull
  public List<String> getInstalledModules() {
    return installedModules;
  }
}
