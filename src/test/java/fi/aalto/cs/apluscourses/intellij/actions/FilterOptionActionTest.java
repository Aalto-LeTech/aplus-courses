package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import org.junit.Test;

import javax.swing.*;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FilterOptionActionTest {

  @Test
  public void testConstructor() {
    String name = "Dummy filter";
    Icon icon = new ImageIcon();
    Option filterOption = new Option(name, icon, item -> Optional.of(true)).init();

    AnAction action = new FilterOptionAction(filterOption);

    assertEquals(name, action.getTemplateText());
    assertEquals(icon, action.getTemplatePresentation().getIcon());
  }

  @Test
  public void testIsSelected() {
    Option filterOption = new Option("My filter", null, item -> Optional.of(true)).init();

    ToggleAction action = new FilterOptionAction(filterOption);

    AnActionEvent e = mock(AnActionEvent.class);

    filterOption.isSelected.set(null);
    assertFalse(action.isSelected(e));

    filterOption.isSelected.set(false);
    assertFalse(action.isSelected(e));

    filterOption.isSelected.set(true);
    assertTrue(action.isSelected(e));
  }

  @Test
  public void testSetSelected() {
    Option filterOption = new Option("Your filter", null, item -> Optional.of(true)).init();

    ToggleAction action = new FilterOptionAction(filterOption);

    AnActionEvent e = mock(AnActionEvent.class);

    action.setSelected(e, false);
    assertEquals(false, filterOption.isSelected.get());

    action.setSelected(e, true);
    assertEquals(true, filterOption.isSelected.get());
  }
}
