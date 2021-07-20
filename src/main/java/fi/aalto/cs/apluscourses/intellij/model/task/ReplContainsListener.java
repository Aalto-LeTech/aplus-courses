package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class ReplContainsListener extends ReplListener {
  private final String @NotNull [] containingInputs;

  @NotNull
  private final String containingOutput;

  /**
   * A constructor with the wanted inputs and output as strings.
   */
  protected ReplContainsListener(@NotNull ListenerCallback callback,
                                 @NotNull Project project,
                                 String @NotNull [] containingInputs,
                                 @NotNull String containingOutput) {
    super(callback, project);
    this.containingInputs = containingInputs;
    this.containingOutput = containingOutput;
  }

  @Override
  protected boolean isCorrect(@NotNull ProcessEvent event) {
    return Arrays.stream(containingInputs).anyMatch(input -> getLastLine().contains(input))
        && getEventText(event).contains(containingOutput);
  }
}
