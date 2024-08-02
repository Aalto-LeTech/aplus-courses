package fi.aalto.cs.apluscourses.utils

//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.openapi.module.ModuleManager;
//import com.intellij.openapi.project.Project;
//import fi.aalto.cs.apluscourses.notifications.MissingFileNotification;
//import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
//import fi.aalto.cs.apluscourses.model.Course;
//import fi.aalto.cs.apluscourses.model.CourseProject;
//import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
//import fi.aalto.cs.apluscourses.model.FileFinder;
//import fi.aalto.cs.apluscourses.model.component.old.OldModule;
//import fi.aalto.cs.apluscourses.model.exercise.Exercise;
//import fi.aalto.cs.apluscourses.model.exercise.SubmissionFileInfo;
//import fi.aalto.cs.apluscourses.model.exercise.SubmissionResult;
//import fi.aalto.cs.apluscourses.presentation.exercise.DownloadSubmissionViewModel;
//import fi.aalto.cs.apluscourses.services.Notifier;
//import fi.aalto.cs.apluscourses.services.PluginSettings;
//import fi.aalto.cs.apluscourses.ui.InstallerDialogs;
//import fi.aalto.cs.apluscourses.utils.APlusLogger;
//import fi.aalto.cs.apluscourses.utils.WindowUtil;
//import java.util.Arrays;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;

class SubmissionDownloader {
//
//  private static final Logger logger = APlusLogger.INSTANCE.getLogger();
//
//  @NotNull
//  private final Interfaces.AuthenticationProvider authenticationProvider;
//
//  @NotNull
//  private final FileFinder fileFinder;
//
//  @NotNull
//  private final Dialogs dialogs;
//
//  // TODO: store language and default group ID in the object model and read them from there
//  private final Interfaces.LanguageSource languageSource;
//
////  @NotNull
////  private final ComponentInstaller.Factory componentInstallerFactory;
//
//  @NotNull
//  private final InstallerDialogs.Factory dialogsFactory;
//  private final Interfaces.FileRefresher fileRefresher;
//  private final Interfaces.FileBrowser fileBrowser;
//  private final Interfaces.VirtualFileFinder virtualFileFinder;
//
//
//  /**
//   * Constructor with reasonable defaults.
//   */
////  public SubmissionDownloader() {
////    this(
////        project -> Optional.ofNullable(PluginSettings.getInstance().getCourseProject(project))
////            .map(CourseProject::getAuthentication).orElse(null),
////        VfsUtil::findFileInDirectory,
////        Dialogs.DEFAULT,
////        project -> PluginSettings.getInstance().getCourseFileManager(project).getLanguage(),
//////        new ComponentInstallerImpl.FactoryImpl<>(new SimpleAsyncTaskManager()),
////        InstallerDialogs::new,
////        Interfaces.FileRefresherImpl::refreshPath,
////        Interfaces.FileBrowserImpl::navigateTo,
////        Interfaces.VirtualFileFinderImpl::findVirtualFile
////    );
////  }
//
//
//  /**
//   * Construct an exercise submission action with the given parameters. This constructor is useful
//   * for testing purposes.
//   */
//  public SubmissionDownloader(@NotNull Interfaces.AuthenticationProvider authenticationProvider,
//                              @NotNull FileFinder fileFinder,
//                              @NotNull Dialogs dialogs,
//                              @NotNull Interfaces.LanguageSource languageSource,
////                              @NotNull ComponentInstaller.Factory componentInstallerFactory,
//                              @NotNull InstallerDialogs.Factory dialogsFactory,
//                              @NotNull Interfaces.FileRefresher fileRefresher,
//                              @NotNull Interfaces.FileBrowser fileBrowser,
//                              @NotNull Interfaces.VirtualFileFinder virtualFileFinder) {
//    this.authenticationProvider = authenticationProvider;
//    this.fileFinder = fileFinder;
//    this.dialogs = dialogs;
//    this.languageSource = languageSource;
////    this.componentInstallerFactory = componentInstallerFactory;
//    this.dialogsFactory = dialogsFactory;
//    this.fileRefresher = fileRefresher;
//    this.fileBrowser = fileBrowser;
//    this.virtualFileFinder = virtualFileFinder;
//  }
//
//  /**
//   * Downloads a submission.
//   **/
//  public void downloadSubmission(@NotNull Project project, @NotNull Course course, @NotNull Exercise exercise,
//                                 @NotNull SubmissionResult submissionResult) {
//    var selectedModule = getSelectedModule(project, course, exercise);
//
//    var installedModules = Arrays
//        .stream(ModuleManager.getInstance(project).getModules())
//        .map(com.intellij.openapi.module.Module::getName)
//        .collect(Collectors.toList());
//
//    var downloadSubmissionViewModel = new DownloadSubmissionViewModel(course, selectedModule, submissionResult.getId(),
//        installedModules);
//
//    WindowUtil.bringWindowToFront(project);
//  WindowManager.getInstance().getFrame(project).toFront()
//
//    if (!dialogs.create(downloadSubmissionViewModel, project).showAndGet()) {
//      return;
//    }
//
//    var module = downloadSubmissionViewModel.selectedModule.get();
//    logger.info("User-selected module: %s".formatted(module));
//    var newName = downloadSubmissionViewModel.moduleName.get();
//    logger.info("New module name: %s".formatted(newName));
//
//    if (module == null || newName == null) {
//      return;
//    }
//
//    var moduleCopy = module.copy(newName);
//
//    var moduleVf = virtualFileFinder.findFile(moduleCopy.getFullPath().toFile());
//
////    componentInstallerFactory.getInstallerFor(course, dialogsFactory.getDialogs(project), course.callbacks)
////        .installAsync(List.of(moduleCopy),
////            () -> fileRefresher.refreshPath(moduleVf,
////                () -> downloadFiles(project,
////                    course,
////                    submissionResult.getFilesInfo().toArray(new SubmissionFileInfo[0]),
////                    moduleCopy)));
//  }
//
//  @Nullable
//  private OldModule getSelectedModule(@NotNull Project project,
//                                      @NotNull Course course,
//                                      @NotNull Exercise exercise) {
//    var language = languageSource.getLanguage(project);
//    logger.info("Selected language: %s".formatted(language));
//
//    var exerciseModules = course.getExerciseModules().get(exercise.getId());
//
//    var selectedComponent = Optional
//        .ofNullable(exerciseModules)
//        .map(self -> self.get(language))
//        .map(course::getComponentIfExists).orElse(null);
//
//    return null;
////    OldModule selectedModule = selectedComponent instanceof OldModule ? (OldModule) selectedComponent : null;
//
////    logger.info("Auto-selected module: %s".formatted(selectedModule));
////    return selectedModule;
//  }
//
//  private void downloadFiles(@Nullable Project project,
//                             @NotNull Course course,
//                             SubmissionFileInfo @NotNull [] submissionFilesInfo,
//                             @NotNull OldModule module) {
//    if (project == null) {
//      logger.info("Project null");
//      return;
//    }
////    course.validate();
//
//    var auth = authenticationProvider.getAuthentication(project);
//
//    logger.info("Starting download");
//    for (var info : submissionFilesInfo) {
//      try {
//        var file = fileFinder.findFile(module.getFullPath(), info.getFileName()).toFile();
//        logger.info("Found file: %s".formatted(file));
////        CoursesClient.fetch(new URL(info.getUrl()), file, auth);
////        logger.info("Fetched file");
////        fileBrowser.navigateTo(file, project);
////        logger.info("Opened file");
//      } catch (FileDoesNotExistException ex) {
//        logger.warn("File not found", ex);
//        Notifier.Companion.notifyAndHide(new MissingFileNotification(module.getPath(), info.getFileName(), true),
//            project, 6000L); //TODO
////      } catch (IOException ex) {
////        logger.warn("IOException while downloading submission", ex);
////        notifier.notifyAndHide(new NetworkErrorNotification(ex), project);
//      }
//    }
//    logger.debug("Finished downloading submission");
//  }
}
