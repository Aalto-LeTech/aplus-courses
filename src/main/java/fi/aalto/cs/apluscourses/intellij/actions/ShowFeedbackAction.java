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

public class ShowFeedbackAction extends AnAction {
  public static final String ACTION_ID = ShowFeedbackAction.class.getCanonicalName();

  private static final String[] SUPPORTED_COURSES = new String[] {"O1", "Programming Studio 2/A"};

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Interfaces.AuthenticationProvider authenticationProvider;

  @Nullable
  private SubmissionResult submissionResult;

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
    var progressViewModel = mainViewModel.progressViewModel;

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

    var progress = progressViewModel.start(1, "Loading Feedback...", true);
    try {

      var feedbackString = exerciseDataSource.getSubmissionFeedback(submissionResult.getId(), authentication);
      var document = Jsoup.parseBodyFragment(feedbackString);

      var textColor = JBColor.black;
      var textColorString =
          String.format("#%02x%02x%02x", textColor.getRed(), textColor.getGreen(), textColor.getBlue());
      var fontName = JBFont.regular().getFontName();

      switch (course.getName()) {
        case "O1":
          var style = document.select("style").first();
          if (style != null) {
            style.text(
                "body {  color: " + textColorString
                    + ";  font-family: \"" + fontName
                    + "\", sans-serif; font-weight: 500; padding: 30px; }"
                    + ".glyphicon:before {  font-weight: bold;  font-size: 20; }"
                    + ".glyphicon-minus:before {  content: \"-\"; }"
                    + ".glyphicon-plus:before {  content: \"+\"; }"
                    + ".label {  display: inline;  padding: .2em .6em .3em;  font-size: 75%;  font-weight: 700;"
                    + "line-height: 1;  color: #fff;  text-align: center;  white-space: nowrap;"
                    + "vertical-align: baseline;  border-radius: .25em; }"
                    + ".label-success {  background-color: #00803c; }"
                    + ".label-danger {  background-color: #a50000; }"
                    + "pre {  display: block;  padding: 10.5px;  margin: 0 0 11px;  font-size: 15px;"
                    + "line-height: 1.428571429;  color: #333;  word-break: break-all;  word-wrap: break-word;"
                    + "background-color: #f5f5f5;  border: 1px solid #ccc;  border-radius: 4px;  width: fit-content; }"
                    + "code { padding: 2px 4px; font-size: 90%; color: #c7254e; background-color: #f9f2f4;"
                    + "border-radius: 4px }"
                    + ".alert {  padding: 15px;  margin-bottom: 22px;  border: 1px solid transparent;"
                    + "border-top-color: transparent;  border-right-color: transparent;"
                    + "border-bottom-color: transparent;  border-left-color: transparent;  border-radius: 4px;"
                    + " width: fit-content; }"
                    + ".alert-warning {  color: #8a6d3b;  background-color: #fcf8e3;  border-color: #faebcc; }"
                    + "ul.last-red>li:last-child {  width: fit-content;  padding: 2;  border-radius: .25em; "
                    + "background-color: #a50000;  color: white; }");
          }
          break;
        case "Programming Studio 2/A":
          document.prepend("<style>body {  color: " + textColorString
              + ";  font-family: \"" + fontName + "\", sans-serif; font-weight: 500; padding: 30px; }"
              + "pre { display: block; padding: 10.5px; margin: 0 0 11px; font-size: 15px; line-height: 1.428571429;"
              + "color: #333; word-break: break-all; word-wrap: break-word; background-color: #f5f5f5;"
              + "border: 1px solid #ccc; border-radius: 4px;}</style>");
          break;
        default:
          return;
      }

      var fileEditorManager = FileEditorManager.getInstance(project);

      Arrays.stream(fileEditorManager.getAllEditors())
          .filter(editor -> editor.getFile() != null && editor.getFile().getName().startsWith("Feedback")).findFirst()
          .ifPresent(editor -> fileEditorManager.closeFile(Objects.requireNonNull(editor.getFile())));
      HTMLEditorProvider.openEditor(project, getAndReplaceText("ui.ShowFeedbackAction.feedbackTitle",
              APlusLocalizationUtil.getEnglishName(submissionResult.getExercise().getName()),
              submissionResult.getId()),
          document.html());
    } catch (IOException ex) {
      notifier.notify(new NetworkErrorNotification(ex), project);
    }
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

    if (courseViewModel == null || exercisesViewModel == null
        || Arrays.stream(SUPPORTED_COURSES).noneMatch(name -> name.equals(courseViewModel.getModel().getName()))) {
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
