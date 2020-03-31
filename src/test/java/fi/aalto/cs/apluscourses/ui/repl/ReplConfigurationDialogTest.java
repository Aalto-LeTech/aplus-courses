package fi.aalto.cs.apluscourses.ui.repl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.aalto.cs.apluscourses.TestHelper;
import org.junit.Test;

public class ReplConfigurationDialogTest extends TestHelper {

  @Test
  public void testCreationWithNoInputWorks() {
    ReplConfigurationDialog dialog = new ReplConfigurationDialog();

    assertTrue(dialog.isModal());
    assertFalse(dialog.isResizable());
    assertEquals("Custom title is set for the dialog.",
        "REPL Configuration", dialog.getTitle());
  }

  @Test
  public void testCreationWithValidFormWorks() {
    //  given
    ReplConfigurationForm form = getDummyReplConfigurationForm();

    //  when
    ReplConfigurationDialog dialog = new ReplConfigurationDialog(form);

    //  then
    assertNotNull(dialog.getReplConfigurationForm());
    assertEquals("Form is now properly set into a dialog.",
        form, dialog.getReplConfigurationForm());
  }

  @Test
  public void testSetReplConfigurationForm() {
    //  given
    ReplConfigurationDialog spy = spy(new ReplConfigurationDialog());
    ReplConfigurationForm form = getDummyReplConfigurationForm();

    //  when
    spy.setReplConfigurationForm(form);

    //  then
    assertSame("Form is now properly set into a dialog.",
        form, spy.getReplConfigurationForm());
    verify(spy, times(1)).replaceReplConfigurationFormWithIn(any(), any());
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