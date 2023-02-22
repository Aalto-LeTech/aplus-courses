package fi.aalto.cs.apluscourses.presentation.base;

import fi.aalto.cs.apluscourses.presentation.ViewModelExtensions.TestNodeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseTreeViewModelTest {

  BaseTreeViewModel<Object> treeViewModel;
  SelectableNodeViewModel<Object> childViewModel1;
  SelectableNodeViewModel<Object> childViewModel2;

  /**
   * Run before each test.
   */
  @BeforeEach
  void setUp() {
    childViewModel1 = new TestNodeViewModel(1, new Object(), Collections.emptyList());
    childViewModel2 = new TestNodeViewModel(2, new Object(), Collections.emptyList());
    treeViewModel = new BaseTreeViewModel<>(
        new Object(), List.of(childViewModel1, childViewModel2), new Options());
  }

  @Test
  void findSelected() {
    childViewModel2.setSelected(true);
    BaseTreeViewModel.Selection selection = treeViewModel.findSelected();

    Assertions.assertSame(treeViewModel, selection.getLevel(0));
    Assertions.assertSame(childViewModel2, selection.getLevel(1));
    Assertions.assertNull(selection.getLevel(2));
  }

  @Test
  void findSelectedReturnsNull() {
    BaseTreeViewModel.Selection selection = treeViewModel.findSelected();

    Assertions.assertNull(selection.getLevel(0));
  }

  @Test
  void setGetSelectedItem() {
    Assertions.assertNull(treeViewModel.getSelectedItem());
    treeViewModel.setSelectedItem(childViewModel1);
    Assertions.assertSame(childViewModel1, treeViewModel.getSelectedItem());
  }
}
