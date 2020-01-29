package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Module;

public class ModulePM implements Selectable {

  private Module module;
  private volatile boolean selected;

  public ModulePM(Module module) {
    this.module = module;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  public String getName() {
    return module.getName();
  }
}
