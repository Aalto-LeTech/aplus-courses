package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import fi.aalto.cs.apluscourses.presentation.filter.Option;
import java.util.Optional;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OptionActionTest {

  @Test
  void testConstructor() {
    String name = "Dummy filter";
    Icon icon = new ImageIcon();
    Option filterOption = new Option(name, icon, item -> Optional.of(true)).init();

    AnAction action = new OptionAction(filterOption);

    Assertions.assertEquals(name, action.getTemplateText());
    Assertions.assertEquals(icon, action.getTemplatePresentation().getIcon());
  }

  @Test
  void testIsSelected() {
    Option filterOption = new Option("My filter", null, item -> Optional.of(true)).init();

    ToggleAction action = new OptionAction(filterOption);

    AnActionEvent e = mock(AnActionEvent.class);

    filterOption.isSelected.set(null);
    Assertions.assertFalse(action.isSelected(e));

    filterOption.isSelected.set(false);
    Assertions.assertFalse(action.isSelected(e));

    filterOption.isSelected.set(true);
    Assertions.assertTrue(action.isSelected(e));
  }

  @Test
  void testSetSelected() {
    Option filterOption = new Option("Your filter", null, item -> Optional.of(true)).init();

    ToggleAction action = new OptionAction(filterOption);

    AnActionEvent e = mock(AnActionEvent.class);

    action.setSelected(e, false);
    Assertions.assertEquals(false, filterOption.isSelected.get());

    action.setSelected(e, true);
    Assertions.assertEquals(true, filterOption.isSelected.get());
  }
}
