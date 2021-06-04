package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbAwareToggleAction;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TutorialActionGroup extends DefaultActionGroup implements DumbAware, Toggleable {
  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  public TutorialActionGroup() {
    this(PluginSettings.getInstance());
  }

  public TutorialActionGroup(@NotNull MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
    var actions = new AnAction[]{new Separator(), new CancelAction()};
    if (e == null) {
      return actions;
    }
    var tutorial = mainViewModelProvider.getMainViewModel(e.getProject()).tutorialViewModel.get();
    if (tutorial == null) {
      return actions;
    }
    var steps = IntStream.range(1, tutorial.getTasksAmount() + 1).mapToObj(StepAction::new).toArray(AnAction[]::new);
    var both = new AnAction[steps.length + 2];
    System.arraycopy(steps, 0, both, 0, steps.length);
    System.arraycopy(actions, 0, both, steps.length, 2);
    return both;
  }

  private class CancelAction extends DumbAwareAction {
    public CancelAction() {
      super("Cancel Tutorial");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      var tutorial = mainViewModelProvider.getMainViewModel(e.getProject()).tutorialViewModel.get();
      if (tutorial != null) {
        tutorial.confirmCancel();
      }
    }
  }

  private class StepAction extends DumbAwareToggleAction {
    private final int index;

    public StepAction(int index) {
      super("Step " + index);
      this.index = index;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
      var tutorial = mainViewModelProvider.getMainViewModel(e.getProject()).tutorialViewModel.get();
      if (tutorial != null) {
        return tutorial.getCurrentTaskIndex() == index;
      }
      return false;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
      //TODO change current task
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      var tutorial = mainViewModelProvider.getMainViewModel(e.getProject()).tutorialViewModel.get();
      if (tutorial != null) {
        e.getPresentation().setEnabled(tutorial.getCurrentTaskIndex() >= index);
        e.getPresentation().setVisible(true);
        setSelected(e, tutorial.getCurrentTaskIndex() == index);
      }
    }
  }
}
