package fi.aalto.cs.apluscourses.ui.repl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import fi.aalto.cs.apluscourses.ui.IconListCellRenderer;
import icons.PluginIcons;
import javax.swing.JList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IconListCellRendererTest {

  @Test
  void testCustomizeWithValidInputWorks() {
    //  given
    IconListCellRenderer renderer = new IconListCellRenderer(PluginIcons.A_PLUS_MODULE);

    //  when
    renderer.customize(new JList<String>(), "", 0, true, true);

    //  then
    Assertions.assertNotNull(renderer.getIcon(), "The icon for rendering Modules in ComboBoxList is set");
    Assertions.assertEquals(PluginIcons.A_PLUS_MODULE, renderer.getIcon(), "There is a correct icon set.");
  }
}
