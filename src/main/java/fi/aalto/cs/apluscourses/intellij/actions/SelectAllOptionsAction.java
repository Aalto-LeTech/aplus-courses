package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.jetbrains.annotations.NotNull;

public class SelectAllOptionsAction extends DumbAwareAction {

  @NotNull
  private final Options options;

  public SelectAllOptionsAction(@NotNull Options options) {
    this.options = options;
    getTemplatePresentation().setText(this::getSelectText);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    options.toggleAll();
  }

  /**
   * The text description for toggling all options.
   */
  public String getSelectText() {
    return options.isAnyActive()
            ? getText("presentation.filter.selectAll")
            : getText("presentation.filter.deselectAll");
  }
}
