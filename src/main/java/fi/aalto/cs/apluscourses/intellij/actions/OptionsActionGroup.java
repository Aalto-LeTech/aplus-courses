package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OptionsActionGroup extends DefaultActionGroup implements DumbAware, Toggleable {

  private Options options = null;
  private final Object lock = new Object();

  @Override
  public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
    var options = getOptionsInternal(e);
    return Stream.concat(
              options.stream().map(OptionAction::new),
              Stream.of(new Separator(),
                      new SelectAllOptionsAction(this.options))
            ).toArray(AnAction[]::new);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(isAvailable(e.getProject()));
    Toggleable.setSelected(e.getPresentation(), getOptionsInternal(e).isAnyActive());
  }

  private @NotNull Options getOptionsInternal(@Nullable AnActionEvent e) {
    synchronized (lock) {
      if (options == null) {
        var options = Optional.ofNullable(e)
                .map(AnActionEvent::getProject)
                .map(this::getOptions);
        if (options.isEmpty()) {
          return Options.EMPTY;
        }
        this.options = options.get();
      }
      return options;
    }
  }

  public abstract @Nullable Options getOptions(@Nullable Project project);

  public boolean isAvailable(@Nullable Project project) {
    return true;
  }
}
