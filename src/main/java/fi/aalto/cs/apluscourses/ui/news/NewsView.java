package fi.aalto.cs.apluscourses.ui.news;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import fi.aalto.cs.apluscourses.intellij.actions.APlusAuthenticationAction;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.OpenItemAction;
import fi.aalto.cs.apluscourses.presentation.news.NewsTreeViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.ToolbarPanel;
import fi.aalto.cs.apluscourses.ui.base.TreeView;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewsView implements ToolbarPanel {
  private TreeView newsTree;
  private JLabel emptyText;
  private JPanel basePanel;
  @GuiObject
  private JScrollPane pane;
  @GuiObject
  public JPanel toolbarContainer;
  @GuiObject
  private JLabel title;
  private JPanel cardPanel;
  private final CardLayout cl;
  private final NoTokenMouseAdapter mouseAdapter = new NoTokenMouseAdapter();

  /**
   * Creates an ExerciseView that uses mainViewModel to dynamically adjust its UI components.
   */
  public NewsView() {
    basePanel.putClientProperty(NewsView.class.getName(), this);
    pane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, JBColor.border()));
    cl = (CardLayout) cardPanel.getLayout();
    newsTree.getEmptyText().setText("");
    newsTree.setOpaque(true);
    newsTree.addMouseListener(mouseAdapter);
    emptyText.setText(getText("ui.exercise.ExercisesView.loading"));
    emptyText.setHorizontalAlignment(SwingConstants.CENTER);
    emptyText.setVerticalAlignment(SwingConstants.CENTER);
    var rowHeight = newsTree.getRowHeight();
    // Row height returns <= 0 on some platforms, so a default alternative is needed
    pane.getVerticalScrollBar().setUnitIncrement(rowHeight <= 0 ? 20 : rowHeight);
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
    //TODO fix
    ApplicationManager.getApplication().invokeLater(() -> {
          newsTree.setViewModel(viewModel);
          cl.show(cardPanel, viewModel == null || false ? "LabelCard" :
              "TreeCard");
          if (viewModel == null) {
            return;
          }

          var unread = viewModel.getModel().getNews().stream().filter(news -> !news.isRead()).count();
          title.setText(getTitle() + ((unread > 0) ? " [" + unread + " unread]" : ""));

          if (true) {
            emptyText.setText(getText("ui.module.ModuleListView.turnIntoAPlusProject"));
            if (true) {
              newsTree.getEmptyText().setText(
                  getText("ui.exercise.ExercisesView.allAssignmentsFiltered"));
            } else {
              newsTree.getEmptyText().setText(
                  getText("ui.exercise.ExercisesView.setToken"));
              newsTree.getEmptyText().appendLine(
                  getText("ui.exercise.ExercisesView.setTokenDirections"));
            }
          }

        }, ModalityState.any()
    );
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    pane = ScrollPaneFactory.createScrollPane(basePanel);
    title = new JLabel();
    newsTree = new TreeView();
    newsTree.setCellRenderer(new NewsTreeRenderer());
    newsTree.addNodeAppliedListener(
        ActionUtil.createOnEventLauncher(OpenItemAction.ACTION_ID, newsTree));
  }

  public TreeView getNewsTree() {
    return newsTree;
  }

  public JLabel getEmptyTextLabel() {
    return emptyText;
  }

  private class NoTokenMouseAdapter extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      if (newsTree.isEmpty()
          && newsTree.getEmptyText().getText().contains(
          getText("ui.exercise.ExercisesView.setToken"))) {
        DataContext context = DataManager.getInstance().getDataContext(e.getComponent());
        ActionUtil.launch(APlusAuthenticationAction.ACTION_ID, context);
      }
    }
  }

  /*private static class ExercisesTreeView extends TreeView {
    @Override
    public void setViewModel(@Nullable BaseTreeViewModel<?> viewModel) {
      var oldViewModel = getViewModel();
      if (viewModel != null && oldViewModel != null) {
        var oldModel = oldViewModel.getModel();
        var newModel = viewModel.getModel();
        if (oldModel instanceof ExercisesTree
            && newModel instanceof ExercisesTree
            && ((ExercisesTree) oldModel).getSelectedStudent()
            != ((ExercisesTree) newModel).getSelectedStudent()) {
          TreeUtil.collapseAll(this, 1);
        }
      }
      super.setViewModel(viewModel);
    }
  }*/

}
