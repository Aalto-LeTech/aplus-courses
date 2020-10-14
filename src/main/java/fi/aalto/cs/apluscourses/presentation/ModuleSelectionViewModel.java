package fi.aalto.cs.apluscourses.presentation;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.module.Module;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import org.jetbrains.annotations.NotNull;

public class ModuleSelectionViewModel {

  public final ObservableProperty<Module> selectedModule = new ObservableReadWriteProperty<>(null);

  private final Module[] modules;

  private final String infoText;

  public ModuleSelectionViewModel(@NotNull Module[] modules, @NotNull String infoText) {
    this.modules = modules;
    this.infoText = infoText;
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
}
