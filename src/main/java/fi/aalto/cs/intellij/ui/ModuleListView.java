package fi.aalto.cs.intellij.ui;

import com.intellij.ui.components.JBList;
import fi.aalto.cs.intellij.presentation.ModuleListPM;
import fi.aalto.cs.intellij.presentation.ModulePM;

public class ModuleListView {
  private JBList<ModulePM> moduleViewList;

  public ModuleListView(ModuleListPM moduleListPM) {
    moduleViewList = new JBList<>(moduleListPM);
    moduleViewList.setSelectionModel(moduleListPM.getSelectionModel());
  }
}
