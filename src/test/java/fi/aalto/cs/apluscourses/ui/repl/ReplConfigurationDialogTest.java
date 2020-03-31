package fi.aalto.cs.apluscourses.ui.repl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.Test;

public class ReplConfigurationDialogTest {

  @Test
  public void testSetReplConfigurationForm() {
    ReplConfigurationDialog replConfigurationDialog = new ReplConfigurationDialog();
  }

  @Test
  public void testReplaceReplConfigurationFormWithIn() {
  }

  @Test
  public void testOnOk() {
    ReplConfigurationDialog replConfigurationDialog = new ReplConfigurationDialog();
    ReplConfigurationForm replConfigurationFormSpy = spy(
        replConfigurationDialog.getReplConfigurationForm());
    doNothing().when(replConfigurationFormSpy).updateModel();

    replConfigurationDialog.onOk();

//    verify(replConfigurationDialog.getReplConfigurationForm(), times(1)).updateModel();
  }
}