package fi.aalto.cs.apluscourses.presentation.exercise;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import org.jetbrains.annotations.NotNull;

public class SubmitExerciseViewModel extends SelectableNodeViewModel<Object> {
  protected SubmitExerciseViewModel() {
    super(new Object(), null);
  }

  @NotNull
  public String getPresentableName() {
    return getText("presentation.submitExerciseViewModel.name");
  }

  @Override
  public long getId() {
    return -1;
  }
}
