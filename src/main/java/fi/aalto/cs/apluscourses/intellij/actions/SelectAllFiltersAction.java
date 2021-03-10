package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import org.jetbrains.annotations.NotNull;

public class SelectAllFiltersAction extends DumbAwareAction {

  @NotNull
  private final Options filterOptions;

  public SelectAllFiltersAction(@NotNull Options filterOptions) {
    super(filterOptions::getSelectText, () -> null, null);
    this.filterOptions = filterOptions;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    filterOptions.toggleAll();
  }
}
