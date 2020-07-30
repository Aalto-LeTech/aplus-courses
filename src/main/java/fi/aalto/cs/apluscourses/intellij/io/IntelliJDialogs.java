package fi.aalto.cs.apluscourses.intellij.io;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import fi.aalto.cs.apluscourses.presentation.dialogs.Dialog;
import fi.aalto.cs.apluscourses.presentation.exercise.SubmissionViewModel;
import fi.aalto.cs.apluscourses.ui.APlusAuthenticationView;
import fi.aalto.cs.apluscourses.ui.exercise.ModuleSelectionDialog;
import fi.aalto.cs.apluscourses.ui.exercise.SubmissionDialog;
import fi.aalto.cs.apluscourses.utils.FactorySelector;

public class IntelliJDialogs extends FactorySelector<Project, Dialog> {
  public static final IntelliJDialogs DEFAULT = new IntelliJDialogs();

  static {
    DEFAULT.register(ModuleSelectionViewModel.class, ModuleSelectionDialog::new);
    DEFAULT.register(SubmissionViewModel.class, SubmissionDialog::new);
    DEFAULT.register(AuthenticationViewModel.class, APlusAuthenticationView::new);
  }

  public interface Factory<T> extends FactorySelector.Factory<T, Project, Dialog> {

  }
}
