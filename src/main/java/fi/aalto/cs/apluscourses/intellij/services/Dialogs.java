package fi.aalto.cs.apluscourses.intellij.services;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.FileSaveViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.APlusAuthenticationView;
import fi.aalto.cs.apluscourses.ui.Dialog;
import fi.aalto.cs.apluscourses.ui.FileSaveView;
import fi.aalto.cs.apluscourses.ui.exercise.ModuleSelectionDialog;
import fi.aalto.cs.apluscourses.ui.exercise.SubmissionDialog;
import fi.aalto.cs.apluscourses.utils.FactorySelector;

public class Dialogs extends FactorySelector<Project, Dialog> {
  public static final Dialogs DEFAULT = new Dialogs();

  static {
    DEFAULT.register(ModuleSelectionViewModel.class, ModuleSelectionDialog::new);
    DEFAULT.register(SubmissionViewModel.class, SubmissionDialog::new);
    DEFAULT.register(AuthenticationViewModel.class, APlusAuthenticationView::new);
    DEFAULT.register(FileSaveViewModel.class, FileSaveView::new);
  }

  public interface Factory<T> extends FactorySelector.Factory<T, Project, Dialog> {

  }
}
