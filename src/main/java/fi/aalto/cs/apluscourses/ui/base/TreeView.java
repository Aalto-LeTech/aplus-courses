package fi.aalto.cs.apluscourses.ui.base;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TreeView extends com.intellij.ui.treeStructure.Tree {

  private static final TreeModelBuilder<SelectableNodeViewModel<?>> TREE_MODEL_BUILDER =
      new TreeModelBuilder<SelectableNodeViewModel<?>>() {
        @Override
        protected @NotNull Stream<? extends SelectableNodeViewModel<?>> childrenOf(
            @NotNull SelectableNodeViewModel<?> obj) {
          return obj.streamVisibleChildren();
        }
      };

  private final transient Set<ActionListener> nodeAppliedListeners = ConcurrentHashMap.newKeySet();
  private final transient Object popupMenuLock = new Object();
  private JPopupMenu popupMenu;

  @NotNull
  public static SelectableNodeViewModel<?> getViewModel(Object node) {
    return (SelectableNodeViewModel<?>) TREE_MODEL_BUILDER.getUserObject(node);
  }

  private transient BaseTreeViewModel<?> viewModel = null;
  private final transient Object viewModelLock = new Object();

  /**
   * Construct an empty tree with no nodes and the root set to invisible.
   */
  public TreeView() {
    setRootVisible(false);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    addTreeSelectionListener(new SelectionListener());
    addMouseListener(new TreeMouseListener());
    getEmptyText().appendLine(getText("ui.module.ModuleListView.turnIntoAPlusProject"));
    getEmptyText().appendLine(getText("ui.module.ModuleListView.directionsAPlusProject"));
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
  public void setViewModel(@Nullable BaseTreeViewModel<?> viewModel) {
    if (viewModel != null) {
      synchronized (viewModelLock) {
        unregisterViewModel();
        this.viewModel = viewModel;
        registerViewModel();
      }
      update();
      viewModel.setSelectedItem(null);
    }
  }

  private void unregisterViewModel() {
    synchronized (viewModelLock) {
      if (viewModel != null) {
        viewModel.filtered.removeCallback(this);
      }
    }
  }

  private void registerViewModel() {
    synchronized (viewModelLock) {
      if (viewModel != null) {
        viewModel.filtered.addListener(this, TreeView::update, SwingUtilities::invokeLater);
      }
    }
  }

  private void update() {
    BaseTreeViewModel<?> localViewModel;
    synchronized (viewModelLock) {
      localViewModel = this.viewModel;
    }
    Set<String> expandedState = getExpandedState();
    setModel(TREE_MODEL_BUILDER.build(localViewModel));
    restoreExpandedState(expandedState);
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
    public void mousePressed(MouseEvent mouseEvent) {
      if (mouseEvent.isPopupTrigger()) {
        showPopupMenu(mouseEvent.getPoint());
      }
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
      if (e.getClickCount() == 2 && viewModel.getSelectedItem() != null) {
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
}
