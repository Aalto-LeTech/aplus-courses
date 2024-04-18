package fi.aalto.cs.apluscourses.intellij.utils;

import fi.aalto.cs.apluscourses.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJFilterOption extends Option {

  private final PluginSettings.LocalIdeSettingsNames setting;
  private final PluginSettings.PropertiesManager propertiesManager;

  /**
   * An option, that is, a filter that when not selected, filters out those that match to the filter
   * given to this constructor.
   */
  public IntelliJFilterOption(@NotNull PluginSettings.PropertiesManager propertiesManager,
                              @NotNull PluginSettings.LocalIdeSettingsNames setting,
                              @NotNull String name,
                              @Nullable Icon icon,
                              @NotNull Filter filter) {
    super(name, icon, filter);
    this.setting = setting;
    this.propertiesManager = propertiesManager;
    isSelected.addValueObserver(this, IntelliJFilterOption::selectionChanged);
  }

  @Override
  public IntelliJFilterOption init() {
    isSelected.set(propertiesManager.getBoolean(setting.getId(), true), this);
    return this;
  }

  private void selectionChanged(@Nullable Boolean value) {
    if (value != null) { // if initialized
      propertiesManager.setValue(setting.getId(), value, true);
    }
  }
}
