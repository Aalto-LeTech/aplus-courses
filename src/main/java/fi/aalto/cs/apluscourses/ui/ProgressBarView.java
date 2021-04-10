package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBColor;
import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;

public class ProgressBarView {
  public final Bindable<JProgressBar, Integer> maxBindable;
  public final Bindable<JProgressBar, Integer> valueBindable;
  public final Bindable<JProgressBar, Boolean> indeterminateBindable;
  public final Bindable<JLabel, String> labelBindable;
  public final Bindable<JPanel, Boolean> visibilityBindable;

  private final JPanel panel;

  /**
   * Creates a progress bar view.
   */
  public ProgressBarView() {
    var progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
    var label = new JLabel();
    panel = new JPanel();

    var border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    progressBar.setBorder(border);
    label.setBorder(border);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
        BorderFactory.createEmptyBorder(0, 0, 10, 0))
    );

    progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.add(label);
    panel.add(progressBar);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    this.maxBindable = new Bindable<>(progressBar,
        (component, value) -> SwingUtilities.invokeLater(() -> component.setMaximum(value)));
    this.valueBindable = new Bindable<>(progressBar,
        (component, value) -> SwingUtilities.invokeLater(() -> component.setValue(value)));
    this.indeterminateBindable = new Bindable<>(progressBar,
        (component, value) -> SwingUtilities.invokeLater(() -> component.setIndeterminate(value)));
    this.labelBindable = new Bindable<>(label,
        (component, value) -> SwingUtilities.invokeLater(() -> component.setText(value)));
    this.visibilityBindable = new Bindable<>(panel,
        (component, value) -> SwingUtilities.invokeLater(() -> component.setVisible(value)));
  }

  @NotNull
  public JPanel getPanel() {
    return this.panel;
  }
}
