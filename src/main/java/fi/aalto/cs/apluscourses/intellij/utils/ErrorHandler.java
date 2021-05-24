package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Consumer;
import java.awt.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ErrorHandler extends ErrorReportSubmitter {
  @Override
  public @Nullable String getPrivacyNoticeText() {
    return "By submitting a report, you agree to the <a href=\"\">privacy policy</a>.";
  }

  @Override
  public @NotNull String getReportActionText() {
    return "Report to A+ Developers";
  }

  @Override
  public boolean submit(IdeaLoggingEvent @NotNull [] events,
                        @Nullable String additionalInfo,
                        @NotNull Component parentComponent,
                        @NotNull Consumer<? super SubmittedReportInfo> consumer) {
    DataContext context = DataManager.getInstance().getDataContext(parentComponent);
    Project project = CommonDataKeys.PROJECT.getData(context);

    new Task.Backgroundable(project, "Sending error report") {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        ApplicationManager.getApplication().invokeLater(() -> {
          Messages.showInfoMessage(parentComponent, "Thank you for submitting the report.", "Error Report");
          consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
        });
      }
    }.queue();
    return true;
  }
}
