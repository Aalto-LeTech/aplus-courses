package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;
import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;
import static icons.PluginIcons.ACCENT_COLOR;

import com.intellij.history.LocalHistory;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.model.ProjectModuleSource;
import fi.aalto.cs.apluscourses.intellij.notifications.DefaultNotifier;
import fi.aalto.cs.apluscourses.intellij.notifications.ExerciseNotSelectedNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingFileNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.MissingModuleNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NetworkErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotSubmittableNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.notifications.SubmissionSentNotification;
import fi.aalto.cs.apluscourses.intellij.services.DefaultGroupIdSetting;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.Interfaces;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
import fi.aalto.cs.apluscourses.model.FileFinder;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionStatusUpdater;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.model.tutorial.Tutorial;
import fi.aalto.cs.apluscourses.model.TutorialExercise;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.ProgressViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseGroupViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionResultViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TutorialViewModel;
import fi.aalto.cs.apluscourses.ui.DuplicateSubmissionDialog;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SubmitExerciseAction extends AnAction {

  private static final Logger logger = APlusLogger.logger;

  public static final String ACTION_ID = SubmitExerciseAction.class.getCanonicalName();

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  @NotNull
  private final Interfaces.AuthenticationProvider authenticationProvider;

  @NotNull
  private final FileFinder fileFinder;

  @NotNull
  private final ProjectModuleSource moduleSource;

  @NotNull
  private final Dialogs dialogs;

  @NotNull
  private final Notifier notifier;

  @NotNull
  private final Interfaces.Tagger tagger;

  @NotNull
  private final Interfaces.DocumentSaver documentSaver;

  // TODO: store language and default group ID in the object model and read them from there
  private final Interfaces.LanguageSource languageSource;

  private final DefaultGroupIdSetting defaultGroupIdSetting;

  @NotNull
  private final Interfaces.ModuleDirGuesser moduleDirGuesser;

  @NotNull
  private final Interfaces.DuplicateSubmissionChecker duplicateChecker;

  @NotNull
  private final Interfaces.SubmissionGroupSelector groupSelector;

  /**
   * Constructor with reasonable defaults.
   */
  public SubmitExerciseAction() {
    this(
        PluginSettings.getInstance(),
        project -> Optional.ofNullable(PluginSettings.getInstance().getCourseProject(project))
            .map(CourseProject::getAuthentication).orElse(null),
        VfsUtil::findFileInDirectory,
        new ProjectModuleSource(),
        Dialogs.DEFAULT,
        new DefaultNotifier(),
        LocalHistory.getInstance()::putSystemLabel,
        FileDocumentManager.getInstance()::saveAllDocuments,
        project -> PluginSettings.getInstance().getCourseFileManager(project).getLanguage(),
        PluginSettings.getInstance(),
        ProjectUtil::guessModuleDir,
        new Interfaces.DuplicateSubmissionCheckerImpl(),
        new Interfaces.SubmissionGroupSelectorImpl()
    );
  }

  /**
   * Construct an exercise submission action with the given parameters. This constructor is useful
   * for testing purposes.
   */
  public SubmitExerciseAction(@NotNull MainViewModelProvider mainViewModelProvider,
                              @NotNull Interfaces.AuthenticationProvider authenticationProvider,
                              @NotNull FileFinder fileFinder,
                              @NotNull ProjectModuleSource moduleSource,
                              @NotNull Dialogs dialogs,
                              @NotNull Notifier notifier,
                              @NotNull Interfaces.Tagger tagger,
                              @NotNull Interfaces.DocumentSaver documentSaver,
                              @NotNull Interfaces.LanguageSource languageSource,
                              @NotNull DefaultGroupIdSetting defaultGroupIdSetting,
                              @NotNull Interfaces.ModuleDirGuesser moduleDirGuesser,
                              @NotNull Interfaces.DuplicateSubmissionChecker duplicateChecker,
                              @NotNull Interfaces.SubmissionGroupSelector groupSelector) {
    this.mainViewModelProvider = mainViewModelProvider;
    this.authenticationProvider = authenticationProvider;
    this.fileFinder = fileFinder;
    this.moduleSource = moduleSource;
    this.dialogs = dialogs;
    this.notifier = notifier;
    this.tagger = tagger;
    this.documentSaver = documentSaver;
    this.languageSource = languageSource;
    this.defaultGroupIdSetting = defaultGroupIdSetting;
    this.moduleDirGuesser = moduleDirGuesser;
    this.duplicateChecker = duplicateChecker;
    this.groupSelector = groupSelector;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(project);
    ProgressViewModel progressViewModel = mainViewModel.progressViewModel;
    if (exercisesViewModel == null) {
      e.getPresentation().setEnabled(false);
    } else {
      var selectedItem = exercisesViewModel.getSelectedItem();
      var isSubmittableExerciseSelected = selectedItem instanceof ExerciseViewModel
          && ((ExerciseViewModel) selectedItem).isSubmittable();
      var isSubmittableSubmissionSelected = selectedItem instanceof SubmissionResultViewModel
          && ((SubmissionResultViewModel) selectedItem).getModel().getExercise()
          .isSubmittable();
      e.getPresentation().setEnabled(project != null
          && authentication != null && courseViewModel != null
          && (isSubmittableExerciseSelected || isSubmittableSubmissionSelected));
      if (progressViewModel.getCurrentProgress() != null && !progressViewModel.getCurrentProgress().isFinished()) {
        e.getPresentation().setText(getText("intellij.actions.SubmitExerciseAction.waitForAssignments"));
      } else {
        e.getPresentation().setText(getText("intellij.actions.SubmitExerciseAction.submitAssignment"));
      }
      var selectedEx = exercisesViewModel.findSelected().getLevel(2);
      var isTutorial = selectedEx instanceof ExerciseViewModel
          && ExerciseViewModel.Status.TUTORIAL.equals(((ExerciseViewModel) selectedEx).getStatus());
      e.getPresentation().setVisible(!isTutorial);
    }
    if ((ActionPlaces.TOOLWINDOW_POPUP).equals(e.getPlace()) && !e.getPresentation().isEnabled()) {
      e.getPresentation().setVisible(false);
    }
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    logger.debug("Starting SubmitExerciseAction");
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    try {
      trySubmit(project);
    } catch (IOException ex) {
      notifyNetworkError(ex, project);
    } catch (FileDoesNotExistException ex) {
      notifier.notify(new MissingFileNotification(ex.getPath(), ex.getName()), project);
    } catch (ModuleMissingException ex) {
      notifier.notify(new MissingModuleNotification(ex.getModuleName()), project);
    }
  }

  private void trySubmit(@NotNull Project project)
      throws IOException, FileDoesNotExistException, ModuleMissingException {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(project);

    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      if (authentication == null) {
        logger.warn("Null authentication while submitting exercise");
      }
      return;
    }

    var selection = (ExercisesTreeViewModel.ExerciseTreeSelection) exercisesViewModel.findSelected();
    ExerciseViewModel selectedExercise = selection.getExercise();
    ExerciseGroupViewModel selectedExerciseGroup = selection.getExerciseGroup();
    if (selectedExercise == null || selectedExerciseGroup == null) {
      notifier.notifyAndHide(new ExerciseNotSelectedNotification(), project);
      return;
    }

    Exercise exercise = selectedExercise.getModel();
    logger.info("Submitting {}", exercise);
    var submissionInfo = exercise.getSubmissionInfo();
    String language = languageSource.getLanguage(project);
    logger.info("Language: {}", language);

    if (!submissionInfo.isSubmittable(language)) {
      logger.warn("{} not submittable", exercise);
      notifier.notify(new NotSubmittableNotification(), project);
      return;
    }

    Map<String, String> exerciseModules =
        courseViewModel.getModel().getExerciseModules().get(exercise.getId());

    Optional<String> moduleName = Optional
        .ofNullable(exerciseModules)
        .map(self -> self.get(language));

    Module selectedModule;
    VirtualFile moduleDir = null;
    if (moduleName.isPresent()) {
      selectedModule = moduleName
          .map(self -> moduleSource.getModule(project, self))
          .orElseThrow(() -> new ModuleMissingException(moduleName.get()));
    } else {
      Module[] modules = moduleSource.getModules(project);

      ModuleSelectionViewModel moduleSelectionViewModel = new ModuleSelectionViewModel(
          modules, getText("ui.toolWindow.subTab.exercises.submission.selectModule"), project, moduleDirGuesser);
      if (!dialogs.call(moduleSelectionViewModel, project).showAndGet()) {
        return;
      }
      selectedModule = moduleSelectionViewModel.selectedModule.get();
      moduleDir = moduleSelectionViewModel.selectedModuleFile.get();
    }

    logger.info("Selected {}", selectedModule);

    if (selectedModule == null) {
      return;
    }

    documentSaver.saveAllDocuments();

    Path modulePath =
        moduleDir == null ? Paths.get(ModuleUtilCore.getModuleDirPath(selectedModule)) : Paths.get(moduleDir.getPath());
    Map<String, Path> files = new HashMap<>();
    for (SubmittableFile file : submissionInfo.getFiles(language)) {
      files.put(file.getKey(), fileFinder.findFile(modulePath, file.getName()));
    }
    logger.info("Submission files: {}", files);

    var course = courseViewModel.getModel();
    var exerciseDataSource = course.getExerciseDataSource();

    List<Group> groups = new ArrayList<>(exerciseDataSource.getGroups(course, authentication));
    groups.add(0, Group.GROUP_ALONE);

    // Find the group from the available groups that matches the default group ID.
    // A group could be removed, so this way we check that the default group ID is still valid.
    Optional<Long> defaultGroupId = defaultGroupIdSetting.getDefaultGroupId();
    final Group defaultGroup = defaultGroupId
        .flatMap(id -> groups
            .stream()
            .filter(group -> group.getId() == id)
            .findFirst())
        .orElse(null);

    final String lastSubmittedGroupId =
        groupSelector.getLastSubmittedGroupId(project, course.getId(), exercise.getId());
    final Group lastSubmittedGroup = groups
        .stream()
        .filter(g -> g.getMemberwiseId().equals(lastSubmittedGroupId))
        .findFirst()
        .orElse(null);

    final SubmissionViewModel submission = new SubmissionViewModel(exercise, groups, defaultGroup,
        lastSubmittedGroup, files, language);

    if (!dialogs.call(submission, project).showAndGet()) {
      return;
    }

    if (duplicateChecker.isDuplicateSubmission(project, course.getId(), exercise.getId(), files)
        && !DuplicateSubmissionDialog.showDialog()) {
      return;
    }

    Group selectedGroup = Objects.requireNonNull(submission.selectedGroup.get());

    if (Boolean.TRUE.equals(submission.makeDefaultGroup.get())) {
      defaultGroupIdSetting.setDefaultGroupId(selectedGroup.getId());
    } else {
      defaultGroupIdSetting.clearDefaultGroupId();
    }

    logger.info("Submitting with group: {}", selectedGroup);
    String submissionUrl = exerciseDataSource.submit(submission.buildSubmission(), authentication);
    logger.info("Submission url: {}", submissionUrl);

    groupSelector.onAssignmentSubmitted(project, course.getId(), exercise.getId(), selectedGroup);
    duplicateChecker.onAssignmentSubmitted(project, course.getId(), exercise.getId(), files);

    new SubmissionStatusUpdater(
        project, exerciseDataSource, authentication, submissionUrl, selectedExercise.getModel(), course
    ).start();
    notifier.notifyAndHide(new SubmissionSentNotification(), project);

    String tag = getAndReplaceText("ui.localHistory.submission.tag",
        selectedExerciseGroup.getPresentableName(),
        submission.getPresentableExerciseName(),
        submission.getCurrentSubmissionNumber());
    addLocalHistoryTag(project, tag);
    logger.debug("Finished submitting exercise");
  }

  /**
   * Submits a tutorial.
   */
  public void submitTutorial(@NotNull Project project,
                             @NotNull TutorialViewModel tutorialViewModel) {
    try {
      trySubmitTutorial(project, tutorialViewModel);
    } catch (IOException ex) {
      notifyNetworkError(ex, project);
    }
  }

  private void trySubmitTutorial(@NotNull Project project,
                                 @NotNull TutorialViewModel tutorialViewModel)
      throws IOException {
    MainViewModel mainViewModel = mainViewModelProvider.getMainViewModel(project);
    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    Authentication authentication = authenticationProvider.getAuthentication(project);

    if (courseViewModel == null || authentication == null) {
      return;
    }

    Tutorial tutorial = tutorialViewModel.getTutorial();
    TutorialExercise exercise = tutorialViewModel.getExercise();

    // a submission for an IDE activity only has one file with a magic file name
    var tutorialResultFile = FileUtilRt.createTempFile(Tutorial.TUTORIAL_SUBMIT_FILE_NAME, null);
    String payload = tutorial.getSubmissionPayload();
    FileUtils.writeStringToFile(tutorialResultFile, payload, StandardCharsets.UTF_8);

    var submissionInfo = exercise.getSubmissionInfo();
    String language = "en";

    if (!submissionInfo.isSubmittable(language)) {
      logger.warn("Tutorial {} not submittable", exercise);
      notifier.notify(new NotSubmittableNotification(), project);
      return;
    }
    var submittableFiles = submissionInfo.getFiles(language);
    if (submittableFiles.size() != 1) {
      logger.warn("Tutorial {} doesn't have one submittable file", exercise);
      return;
    }
    Map<String, Path> files = Map.of(submittableFiles.get(0).getKey(), tutorialResultFile.toPath());

    // IDE activities are always submitted alone
    List<Group> groups = List.of(Group.GROUP_ALONE);

    SubmissionViewModel submission = new SubmissionViewModel(exercise, groups,
        Group.GROUP_ALONE, null, files, language);

    final var exerciseDataSource = courseViewModel.getModel().getExerciseDataSource();
    String submissionUrl = exerciseDataSource.submit(submission.buildSubmission(), authentication);
    logger.info("Tutorial submission url: {}", submissionUrl);

    new SubmissionStatusUpdater(project, exerciseDataSource, authentication, submissionUrl, exercise,
        courseViewModel.getModel()).start();
    notifier.notifyAndHide(new SubmissionSentNotification(), project);
  }

  private void notifyNetworkError(@NotNull IOException exception, @Nullable Project project) {
    logger.warn("Network error while submitting exercise", exception);
    notifier.notify(new NetworkErrorNotification(exception), project);
  }

  private void addLocalHistoryTag(@NotNull Project project, @NotNull String tag) {
    tagger.putSystemLabel(project, tag, ACCENT_COLOR);
  }

  private static class ModuleMissingException extends Exception {

    @NotNull
    private final String moduleName;

    public ModuleMissingException(@NotNull String moduleName) {
      this.moduleName = moduleName;
    }

    @NotNull
    public String getModuleName() {
      return moduleName;
    }
  }
}
