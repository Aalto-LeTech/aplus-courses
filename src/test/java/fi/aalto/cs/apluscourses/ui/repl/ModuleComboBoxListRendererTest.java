package fi.aalto.cs.apluscourses.ui.repl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.swing.JList;
import org.junit.Test;

public class ModuleComboBoxListRendererTest {

  @Test
  public void testCustomizeWithValidInputWorks() {
    //  given
    ModuleComboBoxListRenderer renderer = new ModuleComboBoxListRenderer();

    //  when
    renderer.customize(new JList<String>(), "", 0, true, true);

    //  then
    assertNotNull("The icon for rendering Modules in ComboBoxList is set",
        renderer.getIcon());
  }

  @Test
  public void testCustomizeWithInvalidInputFails() {
    //  given
    ModuleComboBoxListRenderer renderer = new ModuleComboBoxListRenderer();
    final String initialPath = ModuleComboBoxListRenderer.iconPath;
    ModuleComboBoxListRenderer.iconPath = "/fakePath";

    //  when
    renderer.customize(new JList<String>(), "", 0, true, true);

    //  then
    assertNull("The icon for rendering Modules in ComboBoxList is NOT set",
        renderer.getIcon());
    //  rolling back the initial value
    ModuleComboBoxListRenderer.iconPath = initialPath;
  }
}