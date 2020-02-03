package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.actions.ImportModuleAction;
import fi.aalto.cs.intellij.common.Module;
import fi.aalto.cs.intellij.presentation.common.ListElementModel;
import java.awt.Component;
import java.awt.font.TextAttribute;

public class ModuleListElementModel extends ListElementModel {

  private final Module module;

  public ModuleListElementModel(Module module) {
    this.module = module;
  }

  public String getName() {
    return module.getName();
  }

  public String getUrl() {
    return module.getUrl().toString();
  }

  public int getStatus() {
    return 0;
  }

  @Override
  public void listActionPerformed(Component source) {
    ImportModuleAction.trigger(source);
  }

  public float getFontWeight() {
    return getStatus() == 1 ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR;
  }
}
