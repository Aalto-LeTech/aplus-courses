package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class ReplContainsListener extends ScalaReplListener {
  private final String @NotNull [] containingInputs;

  @NotNull
  private final String containingOutput;

  /**
   * A constructor with the wanted inputs and output as strings.
   */
  protected ReplContainsListener(@NotNull ListenerCallback callback,
                                 @NotNull Project project,
                                 String @NotNull [] containingInputs,
                                 @NotNull String containingOutput,
                                 @NotNull String module) {
    super(callback, project, module);
    this.containingInputs = containingInputs;
    this.containingOutput = containingOutput;
  }

  /**
   * Creates an instance of ReplContainsListener based on the provided arguments.
   */
  public static ReplContainsListener create(ListenerCallback callback,
                                            Project project, Arguments arguments) {
    return new ReplContainsListener(callback, project,
        arguments.getArray("inputs"),
        arguments.getString("output"),
        arguments.getString("module"));
  }

  @Override
  protected boolean isCorrect(@NotNull ProcessEvent event) {
    if (lastInput == null) {
      return false;
    }
    return Arrays.stream(containingInputs).anyMatch(input -> lastInput.contains(input))
        && getEventText(event).contains(containingOutput);
  }
}
