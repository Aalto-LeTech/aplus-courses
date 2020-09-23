package fi.aalto.cs.apluscourses.presentation.base;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import fi.aalto.cs.apluscourses.presentation.filter.Options;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class BaseTreeViewModelTest {

  BaseTreeViewModel<Object> treeViewModel;
  SelectableNodeViewModel<Object> childViewModel1;
  SelectableNodeViewModel<Object> childViewModel2;

  /**
   * Run before each test.
   */
  @Before
  public void setUp() {
    childViewModel1 = new SelectableNodeViewModel<>(new Object(), Collections.emptyList());
    childViewModel2 = new SelectableNodeViewModel<>(new Object(), Collections.emptyList());
    treeViewModel = new BaseTreeViewModel<>(
        new Object(), Arrays.asList(childViewModel1, childViewModel2), new Options());
  }

  @Test
  public void findSelected() {
    childViewModel2.setSelected(true);
    BaseTreeViewModel.Selection selection = treeViewModel.findSelected();

    assertSame(treeViewModel, selection.getLevel(0));
    assertSame(childViewModel2, selection.getLevel(1));
    assertNull(selection.getLevel(2));
  }

  @Test
  public void findSelectedReturnsNull() {
    BaseTreeViewModel.Selection selection = treeViewModel.findSelected();

    assertNull(selection.getLevel(0));
  }
}
