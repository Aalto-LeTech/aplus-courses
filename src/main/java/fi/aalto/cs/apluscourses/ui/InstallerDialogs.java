package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.intellij.utils.EdtTask;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.ComponentInstaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InstallerDialogs implements ComponentInstaller.Dialogs {
  @Nullable
  private final Project project;

  public InstallerDialogs(@Nullable Project project) {
    this.project = project;
  }

  @Override
  public boolean shouldOverwrite(@NotNull Component component) {
    String name = component.getName();
    return new EdtTask<Integer>() {
      @Override
      protected Integer execute() {
        return Messages.showYesNoDialog(project, "CAUTION! Updating a module will overwrite your"
                + " local changes in the module. Are you sure you want to update " + name  + "?",
            name, Messages.getWarningIcon());
      }
    }.executeAndWait() == Messages.YES;
  }

  @FunctionalInterface
  public interface Factory {
    @NotNull
    public ComponentInstaller.Dialogs getDialogs(@Nullable Project project);
  }
}
