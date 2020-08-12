package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.module.Module;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;

public class ModuleSelectionViewModel {

  public final ObservableProperty<Module> selectedModule = new ObservableReadWriteProperty<>(null);
  private final Module[] modules;

  public ModuleSelectionViewModel(Module[] modules) {
    this.modules = modules;
  }

  public Module[] getModules() {
    return modules;
  }

  public String getPrompt() {
    return getText("presentation.moduleSelectionViewModel.prompt");
  }
}
