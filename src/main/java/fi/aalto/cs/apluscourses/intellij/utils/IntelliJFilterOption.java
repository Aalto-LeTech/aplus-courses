package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.ide.util.PropertiesComponent;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.filter.Filter;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJFilterOption extends Option {

  private final PluginSettings.LocalIdeSettingsNames setting;

  /**
   * An option, that is, a filter that when not selected, filters out those that match to the filter
   * given to this constructor.
   *
   */
  public IntelliJFilterOption(@NotNull PluginSettings.LocalIdeSettingsNames setting,
                              @NotNull String name,
                              @Nullable Icon icon,
                              @NotNull Filter filter) {
    super(name, icon, filter, PropertiesComponent.getInstance().getBoolean(setting.getName(),
        true));
    this.setting = setting;
    isSelected.set(PropertiesComponent.getInstance().getBoolean(setting.getName(), true));
    isSelected.addValueObserver(this, IntelliJFilterOption::selectionChanged);
  }

  private void selectionChanged(@Nullable Boolean value) {
    PropertiesComponent.getInstance()
        .setValue(setting.getName(), !Boolean.FALSE.equals(value), true);
  }
}
