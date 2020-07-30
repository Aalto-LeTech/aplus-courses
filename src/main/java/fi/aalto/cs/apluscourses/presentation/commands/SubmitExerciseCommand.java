package fi.aalto.cs.apluscourses.presentation.commands;

import com.intellij.openapi.module.Module;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.FileDoesNotExistException;
import fi.aalto.cs.apluscourses.model.FileFinder;
import fi.aalto.cs.apluscourses.model.Group;
import fi.aalto.cs.apluscourses.model.SubmissionHistory;
import fi.aalto.cs.apluscourses.model.SubmissionInfo;
import fi.aalto.cs.apluscourses.model.SubmittableFile;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.dialogs.Dialogs;
import fi.aalto.cs.apluscourses.presentation.exercise.ExerciseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.presentation.messages.Messenger;
import fi.aalto.cs.apluscourses.presentation.messages.MissingFileMessage;
import fi.aalto.cs.apluscourses.presentation.messages.MissingModuleMessage;
import fi.aalto.cs.apluscourses.presentation.messages.NetworkErrorMessage;
import fi.aalto.cs.apluscourses.presentation.messages.NotSubmittableMessage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubmitExerciseCommand implements Command<SubmitExerciseCommand.Context> {

  @NotNull
  private final FileFinder fileFinder;

  public SubmitExerciseCommand(@NotNull FileFinder fileFinder) {
    this.fileFinder = fileFinder;
  }

  @Override
  public boolean canExecute(@NotNull Context context) {
    return canSubmit(context.getMainViewModel());
  }

  @Override
  public void execute(@NotNull Context context) {
    submit(context.getMainViewModel(),
        context.getModuleSource(),
        context.getDialogs(),
        context.getMessenger());
  }

  public boolean canSubmit(@NotNull MainViewModel mainViewModel) {
    return mainViewModel.courseViewModel.get() != null
        && mainViewModel.exercisesViewModel.get() != null;
  }

  private void submit(@NotNull MainViewModel mainViewModel,
                      @NotNull ModuleSource moduleSource,
                      @NotNull Dialogs dialogs,
                      @NotNull Messenger messenger) {
    try {
      trySubmit(mainViewModel, moduleSource, dialogs, messenger);
    } catch (IOException ex) {
      messenger.show(new NetworkErrorMessage(ex));
    } catch (FileDoesNotExistException ex) {
      messenger.show(new MissingFileMessage(ex.getPath(), ex.getName()));
    } catch (ModuleMissingException ex) {
      messenger.show(new MissingModuleMessage(ex.getModuleName()));
    }
  }

  private void trySubmit(@NotNull MainViewModel mainViewModel,
                         @NotNull ModuleSource moduleSource,
                         @NotNull Dialogs dialogs,
                         @NotNull Messenger messenger)
      throws IOException, FileDoesNotExistException, ModuleMissingException {

    CourseViewModel courseViewModel = mainViewModel.courseViewModel.get();
    ExercisesTreeViewModel exercisesViewModel = mainViewModel.exercisesViewModel.get();
    Authentication authentication = mainViewModel.authentication.get();

    if (courseViewModel == null || exercisesViewModel == null || authentication == null) {
      return;
    }

    ExerciseViewModel selectedExercise = exercisesViewModel.getSelectedExercise();
    if (selectedExercise == null) {
      return;
    }
    Course course = courseViewModel.getModel();
    ExerciseDataSource exerciseDataSource = course.getExerciseDataSource();
    Exercise exercise = selectedExercise.getModel();

    SubmissionInfo submissionInfo = exerciseDataSource.getSubmissionInfo(exercise, authentication);
    if (!submissionInfo.isSubmittable()) {
      messenger.show(new NotSubmittableMessage(exercise));
      return;
    }

    String moduleName = selectModule(course, exercise, moduleSource, dialogs);
    if (moduleName == null) {
      return;
    }
    Path modulePath = moduleSource.findModulePath(moduleName);
    Map<String, Path> filePaths = getFilePaths(submissionInfo.getFiles(), modulePath);

    SubmissionHistory history = exerciseDataSource.getSubmissionHistory(exercise, authentication);

    List<Group> groups = new ArrayList<>(exerciseDataSource.getGroups(course, authentication));
    groups.add(0, new Group(0, Collections.singletonList("Submit alone")));

    SubmissionViewModel submission =
        new SubmissionViewModel(exercise, submissionInfo, history, groups, filePaths);

    if (!dialogs.create(submission).showAndGet()) {
      return;
    }

    exerciseDataSource.submit(submission.buildSubmission(), authentication);
  }

  @Nullable
  private String selectModule(@NotNull Course course,
                              @NotNull Exercise exercise,
                              @NotNull ModuleSource moduleSource,
                              @NotNull Dialogs dialogs) {
    Map<String, String> exerciseModules = course.getExerciseModules().get(exercise.getId());

    Optional<String> moduleName = Optional.ofNullable(exerciseModules).map(self -> self.get("en"));
    if (moduleName.isPresent()) {
      return moduleName.get();
    }

    Module[] modules = moduleSource.getModules();
    ModuleSelectionViewModel moduleSelectionViewModel = new ModuleSelectionViewModel(modules);
    if (!dialogs.create(moduleSelectionViewModel).showAndGet()) {
      return null;
    }

    Module module = moduleSelectionViewModel.selectedModule.get();
    return module == null ? null : module.getName();
  }

  @NotNull
  private Map<String, Path> getFilePaths(@NotNull SubmittableFile[] files, @NotNull Path path)
      throws FileDoesNotExistException {
    Map<String, Path> filePaths = new HashMap<>();
    for (SubmittableFile file : files) {
      filePaths.put(file.getKey(), fileFinder.findFile(path, file.getName()));
    }
    return filePaths;
  }

  public interface Context extends MainViewModelContext {
    @NotNull
    ModuleSource getModuleSource();
  }

  public interface ModuleSource {
    @NotNull
    Module[] getModules();

    @Nullable
    Module getModule(@NotNull String moduleName);

    @NotNull
    Path findModulePath(@NotNull String moduleName) throws ModuleMissingException;
  }

  public static class ModuleMissingException extends Exception {
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
