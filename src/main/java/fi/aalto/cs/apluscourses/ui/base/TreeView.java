package fi.aalto.cs.apluscourses.ui.base;

import com.intellij.util.concurrency.annotations.RequiresEdt;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.ui.utils.TreeModelBuilder;
import fi.aalto.cs.apluscourses.ui.utils.TreeModelTraverser;
import fi.aalto.cs.apluscourses.ui.utils.TreePathEncoder;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TreeView extends com.intellij.ui.treeStructure.Tree {

  private static final TreeModelBuilder<SelectableNodeViewModel<?>> TREE_MODEL_BUILDER =
      new TreeModelBuilder<>() {
        @Override
        protected @NotNull Stream<? extends SelectableNodeViewModel<?>> childrenOf(
            @NotNull SelectableNodeViewModel<?> obj) {
          return obj.streamVisibleChildren();
        }
      };

  private final Set<ActionListener> nodeAppliedListeners = ConcurrentHashMap.newKeySet();
  private final Object popupMenuLock = new Object();
  private JPopupMenu popupMenu;

  @NotNull
  public static SelectableNodeViewModel<?> getViewModel(Object node) {
    return (SelectableNodeViewModel<?>) TREE_MODEL_BUILDER.getUserObject(node);
  }

  protected BaseTreeViewModel<?> getViewModel() {
    return viewModel;
  }

  private BaseTreeViewModel<?> viewModel = null;

  /**
   * Construct an empty tree with no nodes and the root set to invisible.
   */
  public TreeView() {
    setRootVisible(false);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    addTreeSelectionListener(new SelectionListener());
    addMouseListener(new TreeMouseListener());
    addTreeWillExpandListener(new MyTreeWillExpandListener());
  }

  /**
   * Sets the {@link JPopupMenu} that is shown for elements.
   *
   * @param popupMenu {@link JPopupMenu}
   */
  public void setPopupMenu(JPopupMenu popupMenu) {
    synchronized (popupMenuLock) {
      this.popupMenu = popupMenu;
    }
  }

  /**
   * Set the model of the this tree to the given view model, or do nothing if the given view model
   * is {@code null}.
   */
  @RequiresEdt
  public void setViewModel(@Nullable BaseTreeViewModel<?> viewModel) {
    if (viewModel != null) {
      unregisterViewModel();
      this.viewModel = viewModel;
      registerViewModel();
      update();
      viewModel.setSelectedItem(null);
    }
  }

  @RequiresEdt
  private void unregisterViewModel() {
    if (viewModel != null) {
      viewModel.getFilterEngine().filtered.removeListener(this);
    }
  }

  @RequiresEdt
  private void registerViewModel() {
    if (viewModel != null) {
      viewModel.getFilterEngine().filtered.addListener(this, TreeView::update, SwingUtilities::invokeLater);
    }
  }

  @RequiresEdt
  private void update() {
    var expandedState = getExpandedState();
    var selection = getSelectionRows();
    setModel(TREE_MODEL_BUILDER.build(viewModel));
    restoreExpandedState(expandedState);
    setSelectionRows(selection);
  }

  public void addNodeAppliedListener(ActionListener listener) {
    nodeAppliedListeners.add(listener);
  }

  public void removeNodeAppliedListener(ActionListener listener) {
    nodeAppliedListeners.remove(listener);
  }

  protected void showPopupMenu(@NotNull Point location) {
    synchronized (popupMenuLock) {
      if (popupMenu != null) {
        popupMenu.show(this, location.x, location.y);
      }
    }
  }

  @NotNull
  private Set<String> getExpandedState() {
    TreeModel treeModel = getModel();
    return treeModel == null ? Collections.emptySet()
        : new TreeModelTraverser(treeModel).traverse()
        .filter(this::isExpanded)
        .map(new TreePathStringEncoder()::encode)
        .collect(Collectors.toSet());
  }

  private void restoreExpandedState(@NotNull Set<String> expandedState) {
    TreeModel treeModel = getModel();
    if (treeModel != null) {
      TreePathStringEncoder coder = new TreePathStringEncoder();
      new TreeModelTraverser(treeModel).traverse()
          .filter(treePath -> expandedState.contains(coder.encode(treePath)))
          .forEach(treePath -> setExpandedState(treePath, true));
    }
  }

  protected class SelectionListener implements TreeSelectionListener {

    @Override
    public void valueChanged(@NotNull TreeSelectionEvent e) {
      SelectableNodeViewModel<?> oldViewModel = getViewModelFromPath(e.getOldLeadSelectionPath());
      if (oldViewModel != null) {
        oldViewModel.setSelected(false);
      }
      SelectableNodeViewModel<?> newViewModel = getViewModelFromPath(e.getNewLeadSelectionPath());
      if (newViewModel != null) {
        newViewModel.setSelected(true);
      }
      viewModel.setSelectedItem(newViewModel);
    }

    @Nullable
    private SelectableNodeViewModel<?> getViewModelFromPath(@Nullable TreePath treePath) {
      return treePath == null ? null : getViewModel(treePath.getLastPathComponent());
    }
  }

  protected class TreeMouseListener extends MouseAdapter {

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
      if (mouseEvent.isPopupTrigger()) {
        showPopupMenu(mouseEvent.getPoint());
      }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
      if (mouseEvent.isPopupTrigger()) {
        showPopupMenu(mouseEvent.getPoint());
      }
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
      // Double clicking items with no children
      if (e.getClickCount() == 2
          && Optional.ofNullable(viewModel.getSelectedItem())
          .map(SelectableNodeViewModel::getChildren).map(List::isEmpty).orElse(false)) {
        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
        nodeAppliedListeners.forEach(listener -> listener.actionPerformed(actionEvent));
      }
    }
  }

  private static class TreePathStringEncoder extends TreePathEncoder<String> {

    @Override
    protected String emptyCode() {
      return "";
    }

    @Override
    protected String encodeNode(String parentCode, Object node) {
      return parentCode + "/" + getViewModel(node).getId();
    }
  }

  private static class MyTreeWillExpandListener implements TreeWillExpandListener {

    @Override
    public void treeWillExpand(TreeExpansionEvent event) {
      getViewModel(event).willExpand();
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) {
      getViewModel(event).willCollapse();
    }

    private SelectableNodeViewModel<?> getViewModel(TreeExpansionEvent event) {
      return TreeView.getViewModel(event.getPath().getLastPathComponent());
    }
  }
}
