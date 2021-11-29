package fi.aalto.cs.apluscourses.ui.news;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.ToolbarPanel;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewsView implements ToolbarPanel {
  private TreeView newsTree;
  private JPanel basePanel;
  @GuiObject
  private JScrollPane pane;
  @GuiObject
  public JPanel toolbarContainer;
  @GuiObject
  private JLabel title;

  /**
   * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
   */
  public NewsView() {
    basePanel.putClientProperty(NewsView.class.getName(), this);
    pane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, JBColor.border()));
    newsTree.getEmptyText().setText("");
    newsTree.setOpaque(true);
    var rowHeight = newsTree.getRowHeight();
    // Row height returns <= 0 on some platforms, so a default alternative is needed
    pane.getVerticalScrollBar().setUnitIncrement(rowHeight <= 0 ? 20 : rowHeight);
    newsTree.getEmptyText().setText(getText("ui.toolWindow.subTab.news.noNews"));
  }

  @Override
  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  @Override
  @NotNull
  public JPanel getToolbar() {
    return toolbarContainer;
  }

  @NotNull
  @Override
  public String getTitle() {
    return getText("ui.toolWindow.subTab.news.name");
  }

  @Override
  public void onExpandSplitter() {
    // Row height is wrong after loading while not expanded, unless this is called.
    ApplicationManager.getApplication().invokeLater(() -> newsTree.addNotify());
  }

  /**
   * Sets the view model of this view, or does nothing if the given view model is null.
   */
  public void viewModelChanged(@Nullable NewsTreeViewModel viewModel) {
    ApplicationManager.getApplication().invokeLater(() -> {
          newsTree.setViewModel(viewModel);
          if (viewModel == null) {
            return;
          }

          title.setText(viewModel.getTitle());

        }, ModalityState.any()
    );
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    pane = ScrollPaneFactory.createScrollPane(basePanel);
    title = new JLabel();
    newsTree = new TreeView();
    newsTree.setCellRenderer(new NewsTreeRenderer());
//    newsTree.addNodeAppliedListener(
//        ActionUtil.createOnEventLauncher(OpenItemAction.ACTION_ID, newsTree));
  }

  public TreeView getNewsTree() {
    return newsTree;
  }

}
