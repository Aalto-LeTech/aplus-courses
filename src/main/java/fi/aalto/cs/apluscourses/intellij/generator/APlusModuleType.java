package fi.aalto.cs.apluscourses.intellij.generator;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import icons.PluginIcons;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class APlusModuleType extends ModuleType<APlusModuleBuilder> {

  private static final String ID = "APLUS_MODULE_TYPE";

  public APlusModuleType() {
    super(ID);
  }

  public static APlusModuleType getInstance() {
    return (APlusModuleType) ModuleTypeManager.getInstance().findByID(ID);
  }

  @Override
  public @NotNull APlusModuleBuilder createModuleBuilder() {
    return new APlusModuleBuilder();
  }

  @Override
  public @NotNull String getName() {
    return getText("intellij.ProjectBuilder.name");
  }

  @Override
  public @NotNull String getDescription() {
    return getText("intellij.ProjectBuilder.description");
  }

  @Override
  public @NotNull Icon getNodeIcon(boolean isOpened) {
    return PluginIcons.A_PLUS_LOGO_COLOR;
  }
}
