package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Module;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class ModuleListPM extends SelectableListModel<ModulePM> {

  private final List<ModulePM> modulePMs;

  public ModuleListPM(List<Module> modules) {
    modulePMs = modules
        .stream()
        .map(ModulePM::new)
        .collect(Collectors.toList());
    getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
