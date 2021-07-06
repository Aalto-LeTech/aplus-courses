package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DownloadSubmissionViewModel {
  @NotNull
  private final List<Module> modules;

  public final ObservableProperty<Module> selectedModule;

  public final ObservableProperty<String> moduleName;

  /**
   * A constructor.
   */
  public DownloadSubmissionViewModel(@NotNull Course course,
                                     @Nullable Module currentSelectedModule,
                                     long submissionId) {
    this.modules = course.getModules();
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
}
