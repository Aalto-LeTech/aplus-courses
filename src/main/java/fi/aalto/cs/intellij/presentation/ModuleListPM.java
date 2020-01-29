package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Module;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ModuleListPM extends SelectableListModel<ModulePM> {

  private final List<ModulePM> modulePMs;

  public ModuleListPM(@NotNull List<Module> modules) {
    modulePMs = modules
        .stream()
        .map(ModulePM::new)
        .collect(Collectors.toList());
  }

  @NotNull
  public final List<ModulePM> getSelectedElements() {
    return modulePMs
        .stream()
        .filter(ModulePM::isSelected)
        .collect(Collectors.toList());
  }

  @Override
  public int getSize() {
    return modulePMs.size();
  }

  @Override
  public ModulePM getElementAt(int i) {
    return modulePMs.get(i);
  }
}
