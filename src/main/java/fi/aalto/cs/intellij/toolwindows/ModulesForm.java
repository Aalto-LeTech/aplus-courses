package fi.aalto.cs.intellij.toolwindows;

import fi.aalto.cs.intellij.common.Course;
import fi.aalto.cs.intellij.presentation.ModuleListModel;
import fi.aalto.cs.intellij.services.PluginSettings;
import fi.aalto.cs.intellij.ui.module.ModuleListView;
import javax.swing.JPanel;

public class ModulesForm {
  private ModuleListView moduleListView;
  private JPanel toolbarContainer;
  private JPanel basePanel;

  public void init() {
    loadModel(PluginSettings.getInstance().getCurrentlyLoadedCourse());
  }

  // If currently loaded course changes, this method should be called.
  private void loadModel(Course course) {
    moduleListView.setModel(new ModuleListModel(course.getModules()));
  }

  public JPanel getBasePanel() {
    return basePanel;
  }
}
