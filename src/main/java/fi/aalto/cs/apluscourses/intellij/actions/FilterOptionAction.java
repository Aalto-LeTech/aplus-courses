package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import org.jetbrains.annotations.NotNull;

public class FilterOptionAction extends ToggleAction {

  private final Option option;

  public FilterOptionAction(Option option) {
    super(option::getName, option.getIcon());
    this.option = option;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return Boolean.TRUE.equals(option.isSelected.get());
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    option.isSelected.set(state);
  }
}
