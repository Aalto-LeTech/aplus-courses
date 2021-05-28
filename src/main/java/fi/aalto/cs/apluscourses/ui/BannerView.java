package fi.aalto.cs.apluscourses.ui;

import com.intellij.ui.JBColor;
import fi.aalto.cs.apluscourses.BannerViewModel;
import fi.aalto.cs.apluscourses.ui.utils.Bindable;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BannerView {
  private final JPanel container;

  private final Bindable<MyEditorNotificationPanel, String> textBindable;

  private final Bindable<MyEditorNotificationPanel, Color> colorBindable;

  /**
   * Constructs a BannerView with a banner at the top and bottomComponent at the bottom.
   * Binds the text to a view model.
   */
  public BannerView(@NotNull JComponent bottomComponent) {
    this.container = new JPanel(new BorderLayout());
    this.container.putClientProperty(BannerView.class.getName(), this);
    var banner = new MyEditorNotificationPanel();
    banner.setVisible(false);
    banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
            BorderFactory.createEmptyBorder(0, 10, 0, 10))
    );

    this.container.add(BorderLayout.CENTER, bottomComponent);
    this.container.add(BorderLayout.NORTH, banner);

    this.textBindable = new Bindable<>(banner, (panel, text) -> {
      panel.setVisible(text != null);
      panel.setText(text);
    }, true);

    this.colorBindable = new Bindable<>(banner, MyEditorNotificationPanel::setColor, true);
  }

  /**
   * Binds the text of the view model.
   */
  public void viewModelChanged(@Nullable BannerViewModel viewModel) {
    textBindable.bindToSource(null);
    colorBindable.bindToSource(null);
    if (viewModel != null) {
      textBindable.bindToSource(viewModel.text);
      colorBindable.bindToSource(viewModel.color);
    }
  }

  @NotNull
  public JPanel getContainer() {
    return this.container;
  }
}
