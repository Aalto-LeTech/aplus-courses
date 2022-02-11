package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ActivityFactory;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ComponentPresenter;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import fi.aalto.cs.apluscourses.model.task.Reaction;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
      case "null":
        return NullListener.create(callback);
      case "openEditor":
        return OpenFileListener.create(callback, project, arguments);
      case "build":
        return BuildActionListener.create(callback, project, arguments);
      case "run":
        return RunActionListener.create(callback, project, arguments);
      case "classDeclScala":
        return ClassDeclarationListener.create(callback, project, arguments);
      case "functionDefinition":
        return FunctionDefinitionListener.create(callback, project, arguments);
      case "errors":
        return ErrorListener.create(callback, project, arguments);
      case "newObjectAssignmentScala":
        return NewObjectAssignmentListener.create(callback, project, arguments);
      case "methodCall":
        return MethodCallListener.create(callback, project, arguments);
      case "assignStatement":
        return AssignStatementListener.create(callback, project, arguments);
      case "declareVariable":
        return VariableDeclarationListener.create(callback, project, arguments);
      case "comment":
        return CommentListener.create(callback, project, arguments);
      case "openRepl":
        return RunReplListener.create(callback, project, arguments);
      case "replInput":
        return ReplInOutListener.create(callback, project, arguments);
      case "replInputContains":
        return ReplContainsListener.create(callback, project, arguments);
      case "stop":
        return StopListener.create(callback, project, arguments);
      case "line":
        return ScalaLineListener.create(callback, project, arguments);
      default:
        throw new IllegalArgumentException("Unsupported action: '" + action + "'");
    }
  }

  @Override
  public @NotNull ComponentPresenter createPresenter(@NotNull String component,
                                                     @Nullable String instruction,
                                                     @Nullable String info,
                                                     @NotNull Arguments componentArguments,
                                                     @NotNull Arguments actionArguments,
                                                     @NotNull String @NotNull [] assertClosed,
                                                     @NotNull Reaction @NotNull [] reactions,
                                                     boolean isAlreadyCompleted) {
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
    Action[] actions = Arrays.stream(reactions).map(PresenterAction::new).toArray(Action[]::new);
    switch (component) {
      case "projectTree":
        return new ProjectTreePresenter(instruction, info, project, actions);
      case "editor":
        return EditorPresenter.create(instruction, info, project, actionArguments, actions);
      case "repl":
        return ReplPresenter.create(instruction, info, project, actionArguments, actions);
      case "build":
        return new BuildPresenter(instruction, info, project, actions);
      default:
        throw new IllegalArgumentException("Unsupported component: '" + component + "'");
    }
  }

  private static class PresenterAction extends AbstractAction {

    private final Reaction reaction;

    public PresenterAction(@NotNull Reaction reaction) {
      super(reaction.getLabel());
      this.reaction = reaction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      reaction.react();
    }
  }
}
