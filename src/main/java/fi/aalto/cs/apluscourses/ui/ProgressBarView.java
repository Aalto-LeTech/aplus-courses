package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBColor;
import fi.aalto.cs.apluscourses.presentation.ProgressViewModel;
import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;

public class ProgressBarView {
  private final Bindable<JProgressBar, Integer> maxBindable;
  private final Bindable<JProgressBar, Integer> valueBindable;
  private final Bindable<JProgressBar, Boolean> indeterminateBindable;
  private final Bindable<JLabel, String> labelBindable;
  private final Bindable<JPanel, Boolean> visibilityBindable;

  private final JProgressBar progressBar;
  private final JLabel label;
  private final JPanel panel;
  private final JPanel container;
  private final ProgressViewModel viewModel;
  private final JComponent bottomComponent;

  /**
   * Creates a progress bar view. The progress bar is overlaid on top of the bottomComponent.
   */
  public ProgressBarView(@NotNull ProgressViewModel viewModel,
                         @NotNull JComponent bottomComponent) {
    this.viewModel = viewModel;
    this.bottomComponent = bottomComponent;
    this.progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
    this.label = new JLabel();
    this.panel = createProgressBarPanel();
    this.container = createContainer();

    this.maxBindable = new Bindable<>(progressBar, JProgressBar::setMaximum, true);
    this.valueBindable = new Bindable<>(progressBar, JProgressBar::setValue, true);
    this.indeterminateBindable = new Bindable<>(progressBar, JProgressBar::setIndeterminate, true);
    this.labelBindable = new Bindable<>(label, JLabel::setText, true);
    this.visibilityBindable = new Bindable<>(panel, JPanel::setVisible, true);

    setBindings();
  }

  private void setBindings() {
    maxBindable.bindToSource(viewModel.maxValue);
    valueBindable.bindToSource(viewModel.value);
    labelBindable.bindToSource(viewModel.label);
    visibilityBindable.bindToSource(viewModel.visible);
    indeterminateBindable.bindToSource(viewModel.indeterminate);
  }

  private JPanel createProgressBarPanel() {
    var myPanel = new JPanel();
    var border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    progressBar.setBorder(border);
    label.setBorder(border);
    myPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
        BorderFactory.createEmptyBorder(0, 0, 10, 0))
    );

    progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);

    myPanel.add(label);
    myPanel.add(progressBar);
    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

    return myPanel;
  }

  private JPanel createContainer() {
    var myContainer = new JPanel() {
      @Override
      public boolean isOptimizedDrawingEnabled() {
        // This has to be false for the progress bar to be visible while scrolling.
        return false;
      }
    };
    myContainer.setLayout(new OverlayLayout(myContainer));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    bottomComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
    bottomComponent.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    myContainer.add(panel);
    myContainer.add(bottomComponent);

    return myContainer;
  }

  @NotNull
  public JPanel getContainer() {
    return this.container;
  }
}
