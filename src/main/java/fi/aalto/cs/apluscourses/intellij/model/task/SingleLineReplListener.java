package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;

public class SingleLineReplListener extends ReplListener {
  @NotNull
  private final String input;

  @NotNull
  private final String output;
  /**
   * A constructor with the wanted input and output as a string.
   */
  public SingleLineReplListener(@NotNull ListenerCallback callback,
                                @NotNull Project project,
                                @NotNull String input,
                                @NotNull String output) {
    super(callback, project);
    this.input = input.replaceAll("\\s","");
    this.output = output;
  }

  @Override
  protected boolean isCorrect(@NotNull ProcessEvent event) {
    return input.equals(getLastLine()) && (output + "\n").equals(getEventText(event));
  }
}
