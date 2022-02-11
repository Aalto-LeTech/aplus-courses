package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scala.lang.psi.impl.source.ScalaCodeFragment;

public class ScalaLineListener extends LineListener {
  protected ScalaLineListener(@NotNull ListenerCallback callback,
                              @NotNull Project project,
                              @NotNull String filePath,
                              int lineOneBased,
                              @NotNull String expected) {
    super(callback, project, filePath, lineOneBased, expected);
  }

  public static ActivitiesListener create(ListenerCallback callback, Project project, Arguments arguments) {
    return new ScalaLineListener(callback, project,
        arguments.getString("filePath"),
        arguments.getInt("line"),
        arguments.getString("expected"));
  }

  @Override
  protected @NotNull PsiCodeFragment getPsiCodeFragment(@NotNull String text) {
    return ScalaCodeFragment.apply(text, file.getPsiFile(), null, project);
  }
}
