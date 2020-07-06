package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class GetSubmissionsDashboardAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    fetchSubmissionsDashboard();
  }

  public void fetchSubmissionsDashboard(){
    //do stuff
  }
}
