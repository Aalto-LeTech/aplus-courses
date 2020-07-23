package fi.aalto.cs.apluscourses.intellij;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.ui.Dialog;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DialogHelper<T> {

  private final Function<T, Boolean> func;

  public DialogHelper(Function<T, Boolean> func) {
    this.func = func;
  }

  public boolean showAndGet(T viewModel) {
    return func.apply(viewModel);
  }

  public static class Factory<T> implements Dialogs.Factory<T> {

    private final DialogHelper<T> dialogHelper;
    private final Project expectedProject;

    public Factory(DialogHelper<T> dialogHelper, Project expectedProject) {
      this.dialogHelper = dialogHelper;
      this.expectedProject = expectedProject;
    }

    @NotNull
    @Override
    public Dialog create(@NotNull T viewModel, @Nullable Project project) {
      if (project != expectedProject) {
        throw new IllegalArgumentException();
      }
      return () -> dialogHelper.showAndGet(viewModel);
    }
  }
}
