package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBFont;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class ShowFeedbackAction extends AnAction {
  public static final String ACTION_ID = ShowFeedbackAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Interfaces.AuthenticationProvider authenticationProvider;

  @Nullable
  private SubmissionResult submissionResult = null;

  @NotNull
  private final Notifier notifier;

  /**
   * Default constructor.
   */
  public ShowFeedbackAction() {
    this(
        PluginSettings.getInstance(),
        project -> Optional.ofNullable(PluginSettings.getInstance().getCourseProject(project))
            .map(CourseProject::getAuthentication).orElse(null),
        new DefaultNotifier()
    );
  }

  /**
   * Constructor.
   */
  public ShowFeedbackAction(
      @NotNull MainViewModelProvider mainViewModelProvider,
      @NotNull Interfaces.AuthenticationProvider authenticationProvider,
      @NotNull Notifier notifier) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.authenticationProvider = authenticationProvider;
    this.notifier = notifier;

  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) {
      return;
    }

    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    var exercisesViewModel = mainViewModel.exercisesViewModel.get();
    var courseViewModel = mainViewModel.courseViewModel.get();
    var authentication = authenticationProvider.getAuthentication(project);

    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      return;
    }

    var selection = (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesViewModel.findSelected();
    var selectedExercise = selection.getExercise();
    var selectedSubmission = selection.getSubmissionResult();
    if ((selectedExercise == null || selectedSubmission == null) && submissionResult == null) {
      return;
    }
    if (submissionResult == null) {
      submissionResult = selectedSubmission.getModel();
    }
    var course = courseViewModel.getModel();
    var exerciseDataSource = course.getExerciseDataSource();

    var progress = mainViewModel.progressViewModel.start(1, "Loading Feedback...", true);
    try {
      var feedbackCss = mainViewModel.getFeedbackCss();
      if (feedbackCss == null) {
        return;
      }

      var feedbackString = exerciseDataSource.getSubmissionFeedback(submissionResult.getId(), authentication);
      var document = Jsoup.parseBodyFragment(feedbackString);

      var textColor = JBColor.black;
      var textColorString =
          String.format("#%02x%02x%02x", textColor.getRed(), textColor.getGreen(), textColor.getBlue());
      var backgroundColor = JBColor.background();
      var backgroundColorString =
          String.format("#%02x%02x%02x", backgroundColor.getRed(), backgroundColor.getGreen(),
              backgroundColor.getBlue());
      var fontName = JBFont.regular().getFontName();

      document.head().append("<style>"
          + Jsoup.clean(
          feedbackCss
              .replaceAll("TEXT_COLOR", textColorString)
              .replaceAll("BG_COLOR", backgroundColorString)
              .replaceAll("FONT_NAME", fontName),
          Safelist.none())
          + "</style>");

      var fileEditorManager = FileEditorManager.getInstance(project);

      Arrays.stream(fileEditorManager.getAllEditors())
          .filter(editor -> editor.getFile() != null && editor.getFile().getName().startsWith("Feedback for "))
          .findFirst()
          .ifPresent(editor -> fileEditorManager.closeFile(Objects.requireNonNull(editor.getFile())));
      HTMLEditorProvider.openEditor(project, getAndReplaceText("ui.ShowFeedbackAction.feedbackTitle",
              submissionResult.getExercise().getName(), String.valueOf(submissionResult.getId())),
          document.html());
    } catch (IOException ex) {
      notifier.notify(new NetworkErrorNotification(ex), project);
    }
    submissionResult = null;
    progress.finish();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) {
      return;
    }

    var mainViewModel = mainViewModelProvider.getMainViewModel(project);
    var exercisesViewModel = mainViewModel.exercisesViewModel.get();
    var courseViewModel = mainViewModel.courseViewModel.get();

    if (courseViewModel == null || exercisesViewModel == null || mainViewModel.getFeedbackCss() == null) {
      e.getPresentation().setVisible(false);
      return;
    }
    var selectedSubmissionResult =
        ((ExercisesTreeViewModel.ExerciseTreeSelection) exercisesViewModel.findSelected()).getSubmissionResult();
    if (selectedSubmissionResult == null || !selectedSubmissionResult.getModel().getExercise().isSubmittable()) {
      e.getPresentation().setEnabled(false);
    }
  }

  public void setSubmissionResult(@Nullable SubmissionResult submissionResult) {
    this.submissionResult = submissionResult;
  }
}