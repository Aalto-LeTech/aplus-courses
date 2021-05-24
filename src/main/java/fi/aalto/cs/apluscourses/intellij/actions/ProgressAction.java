package fi.aalto.cs.apluscourses.intellij.actions;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getAndReplaceText;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.jetbrains.annotations.NotNull;

public class ProgressAction extends ComboBoxAction implements DumbAware {

  @NotNull
  private final MainViewModelProvider mainViewModelProvider;

  public ProgressAction() {
    this(PluginSettings.getInstance());
  }

  public ProgressAction(@NotNull MainViewModelProvider mainViewModelProvider) {
    this.mainViewModelProvider = mainViewModelProvider;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {

  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      return;
    }
    var tutorialViewModel =
            mainViewModelProvider.getMainViewModel(e.getProject()).tutorialViewModel.get();
    if (tutorialViewModel == null) {
      e.getPresentation().setVisible(false);
    } else {
      e.getPresentation().setText(getAndReplaceText("presentation.navbar.progress",
              tutorialViewModel.getCurrentTaskIndex(), tutorialViewModel.getTasksAmount()));
    }
  }

  @NotNull
  @Override
  public JComponent createCustomComponent(@NotNull final Presentation presentation,
                                          @NotNull String place) {
    ComboBoxAction.ComboBoxButton button = new ComboBoxAction.ComboBoxButton(presentation) {
      @Override
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, JBUIScale.scale(75));
        return d;
      }


      @Override
      protected void fireActionPerformed(ActionEvent event) {
        //performWhenButton(this, ActionPlaces.UNKNOWN);
      }

      @Override
      protected boolean isArrowVisible(@NotNull Presentation presentation) {
        return false;
      }
    };
    NonOpaquePanel panel = new NonOpaquePanel(new BorderLayout());
    Border border = UIUtil.isUnderDefaultMacTheme()
            ? JBUI.Borders.empty(0, 2) : JBUI.Borders.empty(0, 5, 0, 4);

    panel.setBorder(border);
    panel.add(button);
    return panel;
  }

  @Override
  protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
    return new DefaultActionGroup();
  }

}
