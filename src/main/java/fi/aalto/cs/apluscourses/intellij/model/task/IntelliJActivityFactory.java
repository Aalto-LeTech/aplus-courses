package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ActivityFactory;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ComponentPresenter;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import org.jetbrains.annotations.NotNull;

public class IntelliJActivityFactory implements ActivityFactory {
  private final @NotNull Project project;

  public IntelliJActivityFactory(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public @NotNull ActivitiesListener createListener(@NotNull String action,
                                                    @NotNull Arguments arguments,
                                                    @NotNull ListenerCallback callback) {
    switch (action) {
      case "openEditor":
        return OpenFileListener.create(callback, project, arguments);
      case "build":
        return BuildActionListener.create(callback, project);
      case "run":
        return RunActionListener.create(callback, project, arguments);
      case "classDeclScala":
        return ClassDeclarationListener.create(callback, project, arguments);
      case "functionDefinition":
        return FunctionDefinitionListener.create(callback, project, arguments);
        //TODO
      case "openRepl":
        return new RunReplListener(callback, project,
            arguments.getOrThrow("module"));
      case "replInput":
        return new SingleLineReplListener(callback, project,
            arguments.getOrThrow("input"),
            arguments.getOrThrow("output"));
      case "replInputContains":
        return new ReplContainsListener(callback, project,
            arguments.getArrayOrThrow("inputs"),
            arguments.getOrThrow("output"));
      default:
        throw new IllegalArgumentException("Unsupported action: '" + action + "'");
    }
  }

  @Override
  public @NotNull ComponentPresenter createPresenter(@NotNull String component,
                                                     @NotNull String instruction,
                                                     @NotNull String info,
                                                     @NotNull Arguments componentArguments,
                                                     @NotNull Arguments actionArguments,
                                                     String @NotNull [] assertClosed) {
    for (var closedComponent : assertClosed) {
      switch (closedComponent) {
        case "projectTree":
          ComponentDatabase.hideToolWindow(ComponentDatabase.PROJECT_TOOL_WINDOW, project);
          break;
        case "aPlusCourses":
          ComponentDatabase.hideToolWindow(ComponentDatabase.APLUS_TOOL_WINDOW, project);
          break;
        case "editor":
          ComponentDatabase.closeFile(actionArguments, project);
          break;
        case "repl":
          ComponentDatabase.closeRepls(project);
          break;
        default:
          throw new IllegalArgumentException("Unsupported component: '" + closedComponent + "'");
      }
    }
    switch (component) {
      case "projectTree":
        return new ProjectTreePresenter(instruction, info, project);
      case "editor":
        return EditorPresenter.create(instruction, info, project, actionArguments);
      case "repl":
        //TODO
        return new ReplPresenter(instruction, info, actionArguments.getOrThrow("module"), project);
      default:
        throw new IllegalArgumentException("Unsupported component: '" + component + "'");
    }
  }
}
