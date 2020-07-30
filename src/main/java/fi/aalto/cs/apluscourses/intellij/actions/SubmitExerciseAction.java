package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.presentation.commands.SubmitExerciseCommand;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmitExerciseAction extends CommandAction<SubmitExerciseCommand.Context> {

  public static final String ACTION_ID = SubmitExerciseAction.class.getCanonicalName();

  protected SubmitExerciseAction() {
    super(new SubmitExerciseCommand(VfsUtil::findFileInDirectory));
  }

  @NotNull
  @Override
  protected SubmitExerciseCommand.Context getContext(@NotNull AnActionEvent e) {
    return new ContextImpl(e);
  }

  private static class ContextImpl extends MainViewModelContextImpl
      implements SubmitExerciseCommand.Context {

    protected ContextImpl(AnActionEvent event) {
      super(event);
    }

    @Override
    @NotNull
    public SubmitExerciseCommand.ModuleSource getModuleSource() {
      return new ModuleSource(event.getProject());
    }
  }

  private static class ModuleSource implements SubmitExerciseCommand.ModuleSource {

    @Nullable
    private final Project project;

    public ModuleSource(@Nullable Project project) {
      this.project = project;
    }

    @Override
    @NotNull
    public Module[] getModules() {
      return project == null ? new Module[0] : ModuleManager.getInstance(project).getModules();
    }

    @Override
    @Nullable
    public Module getModule(@NotNull String moduleName) {
      return project == null ? null : ModuleManager.getInstance(project)
          .findModuleByName(moduleName);
    }

    @NotNull
    @Override
    public Path findModulePath(@NotNull String moduleName)
        throws SubmitExerciseCommand.ModuleMissingException {
      return Paths.get(ModuleUtilCore.getModuleDirPath(Optional.ofNullable(getModule(moduleName))
          .orElseThrow(() -> new SubmitExerciseCommand.ModuleMissingException(moduleName))));
    }
  }
}
