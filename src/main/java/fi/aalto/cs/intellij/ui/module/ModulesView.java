package fi.aalto.cs.intellij.ui.module;

import fi.aalto.cs.intellij.presentation.MainModel;
import javax.swing.JPanel;

public class ModulesView {
  private ModuleListView moduleListView;
  private JPanel toolbarContainer;
  private JPanel basePanel;

  public ModulesView() {

  }

  public void setModel(MainModel mainModel) {
    moduleListView.setModel(mainModel.getModules());
  }

  public JPanel getBasePanel() {
    return basePanel;
  }

  public JPanel getToolbarContainer() {
    return toolbarContainer;
  }

  public ModuleListView getModuleListView() {
    return moduleListView;
  }
}
