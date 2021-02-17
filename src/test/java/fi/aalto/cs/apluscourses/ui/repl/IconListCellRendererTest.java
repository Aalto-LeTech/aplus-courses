package fi.aalto.cs.apluscourses.ui.repl;

import fi.aalto.cs.apluscourses.ui.IconListCellRenderer;
import icons.PluginIcons;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IconListCellRendererTest {

  @Test
  public void testCustomizeWithValidInputWorks() {
    //  given
    IconListCellRenderer renderer = new IconListCellRenderer(PluginIcons.A_PLUS_MODULE);

    //  when
    renderer.customize(new JList<String>(), "", 0, true, true);

    //  then
    assertNotNull("The icon for rendering Modules in ComboBoxList is set", renderer.getIcon());
    assertEquals("There is a correct icon set.", PluginIcons.A_PLUS_MODULE, renderer.getIcon());
  }
}
