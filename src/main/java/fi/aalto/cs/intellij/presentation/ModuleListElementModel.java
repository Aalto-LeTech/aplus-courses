package fi.aalto.cs.intellij.presentation;

import fi.aalto.cs.intellij.common.Module;
import fi.aalto.cs.intellij.presentation.common.ListElementModel;
import java.awt.font.TextAttribute;

public class ModuleListElementModel extends ListElementModel {

  private final Module module;
  private volatile String status = "Not installed";

  public ModuleListElementModel(Module module) {
    this.module = module;
  }

  public String getName() {
    return module.getName();
  }

  public String getUrl() {
    return module.getUrl().toString();
  }

  public String getStatus() {
    return status;
  }

  @Override
  public void listActionPerformed() {
    status = "Installed";
    changed();
  }

  public float getFontWeight() {
    return status.equals("Installed") ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR;
  }
}
