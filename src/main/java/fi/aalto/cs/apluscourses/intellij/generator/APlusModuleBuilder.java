package fi.aalto.cs.apluscourses.intellij.generator;

import com.intellij.ide.util.projectWizard.ModuleBuilder;

public class APlusModuleBuilder extends ModuleBuilder {
  @Override
  public APlusModuleType getModuleType() {
    return APlusModuleType.getInstance();
  }

  @Override
  public boolean canCreateModule() {
    return false;
  }

  @Override
  public boolean isOpenProjectSettingsAfter() {
    return true;
  }
}
