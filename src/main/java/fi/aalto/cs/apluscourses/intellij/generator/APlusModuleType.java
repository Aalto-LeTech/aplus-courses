package fi.aalto.cs.apluscourses.intellij.generator;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

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
        return "A+ Project";
    }

    @Override
    public @NotNull String getDescription() {
        return "Description goes here";
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return PluginIcons.A_PLUS_LOGO;
    }
}
