package fi.aalto.cs.apluscourses.ui.base;

import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.ui.utils.TreeModelBuilder;
import fi.aalto.cs.apluscourses.ui.utils.TreeModelTraversal;
import fi.aalto.cs.apluscourses.ui.utils.TreePathCoder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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

  @NotNull
  public static SelectableNodeViewModel<?> getViewModel(Object node) {
    return (SelectableNodeViewModel<?>) TREE_MODEL_BUILDER.getUserObject(node);
  }

  @Nullable
  protected transient volatile SelectableNodeViewModel<?> selectedItem = null; //NOSONAR
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
    }
    selectedItem = null;
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

  private Set<String> getExpandedState() {
    return new TreeModelTraversal(getModel()).traverse()
        .filter(this::isExpanded)
        .map(new MyTreePathCoder()::code)
        .collect(Collectors.toSet());
  }

  private void restoreExpandedState(Set<String> expandedState) {
    MyTreePathCoder coder = new MyTreePathCoder();
    new TreeModelTraversal(getModel()).traverse()
        .filter(treePath -> expandedState.contains(coder.code(treePath)))
        .forEach(treePath -> setExpandedState(treePath, true));
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
      selectedItem = newViewModel;
    }

    @Nullable
    private SelectableNodeViewModel<?> getViewModelFromPath(@Nullable TreePath treePath) {
      return treePath == null ? null : getViewModel(treePath.getLastPathComponent());
    }
  }

  protected class TreeMouseListener extends MouseAdapter {

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
      if (e.getClickCount() == 2 && selectedItem != null) {
        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
        nodeAppliedListeners.forEach(listener -> listener.actionPerformed(actionEvent));
      }
    }
  }

  private class MyTreePathCoder extends TreePathCoder<String> {

    @Override
    protected String emptyCode() {
      return "";
    }

    @Override
    protected String codeNode(String parentCode, Object node) {
      return parentCode + "/" + getViewModel(node).getId();
    }
  }
}
