package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ActivityFactory;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ComponentPresenter;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
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
        return new OpenFileListener(callback, arguments.getOrThrow("filepath"), project);
      case "varRename":
        return new RenameVariableListener(callback, arguments.getOrThrow("filepath"),
                arguments.getOrThrow("oldName"),
                arguments.getOrThrow("newName"), project);
      case "build":
        return new IdeActionListener(callback, project, arguments.getOrThrow("actionName"));
      case "classDeclScala":
        return new ClassDeclarationListener(callback, project, arguments.getOrThrow("className"),
                arguments.getArrayOrThrow("classArguments"), arguments.getArrayOrThrow("classHierarchy"), arguments.getOrThrow("filepath"));
      case "functionDefinition":
        return new FunctionDefinitionListener(callback, project,
                arguments.get("methodName"),
                arguments.getArrayOrThrow("arguments"),
                arguments.getArrayOrThrow("body"),
                arguments.getOrThrow("returnType"),
                arguments.getOrThrow("filepath"));
      default:
        throw new IllegalArgumentException("Unsupported action: " + action);
    }
  }

  @Override
  public @NotNull ComponentPresenter createPresenter(@NotNull String component,
                                                     @NotNull String instruction,
                                                     @NotNull String info,
                                                     @NotNull Arguments arguments) {
    switch (component) {
      case "projectTree":
        return new ProjectTreePresenter(instruction, info);
      default:
        throw new IllegalArgumentException("Unsupported component: " + component);
    }
  }
}
