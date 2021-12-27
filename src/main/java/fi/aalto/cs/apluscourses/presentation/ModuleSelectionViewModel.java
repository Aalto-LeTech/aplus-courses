package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;

public class ModuleSelectionViewModel {

  public final ObservableProperty<Module> selectedModule = new ObservableReadWriteProperty<>(null);

  public final ObservableProperty<VirtualFile> selectedModuleFile = new ObservableReadWriteProperty<>(null);

  private final Module[] modules;

  private final String infoText;
  private final Project project;

  /**
   * Constructor.
   */
  public ModuleSelectionViewModel(@NotNull Module[] modules, @NotNull String infoText, @NotNull Project project,
                                  @NotNull Interfaces.ModuleDirGuesser moduleDirGuesser) {
    this.modules = modules;
    this.infoText = infoText;
    this.project = project;
    selectedModule.addValueObserver(this, (self, module) -> {
      if (module == null) {
        selectedModuleFile.set(null);
      } else {
        var path = moduleDirGuesser.guessModuleDir(module);
        selectedModuleFile.set(path);
      }
    });
  }

  public ModuleSelectionViewModel(@NotNull Module[] modules, @NotNull String infoText, @NotNull Project project) {
    this(modules, infoText, project, ProjectUtil::guessModuleDir);
  }


  public Module[] getModules() {
    return modules;
  }

  public String getInfoText() {
    return infoText;
  }

  public String getPrompt() {
    return getText("presentation.moduleSelectionViewModel.prompt");
  }

  public Project getProject() {
    return project;
  }
}
