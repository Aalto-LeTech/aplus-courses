package fi.aalto.cs.intellij.ui;

import fi.aalto.cs.intellij.presentation.ModulePM;
import javax.swing.JLabel;

public class ModuleView extends JLabel {

  private ModulePM modulePM;

  public ModuleView(ModulePM modulePM) {
    this.modulePM = modulePM;
    setText("Module: " + modulePM.getName());
  }
}
