package fi.aalto.cs.apluscourses.ui.repl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fi.aalto.cs.apluscourses.TestHelper;
import javax.swing.JPanel;
import org.junit.Test;

public class ReplConfigurationDialogTest extends TestHelper {

  @Test
  public void testCreationWithNoInputWorks() {
    //  given & when
    ReplConfigurationDialog dialog = new ReplConfigurationDialog();

    //  then
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
  public void testReplaceReplConfigurationFormWithInValidJPanelFormWorks() {
    //  given
    ReplConfigurationForm replConfigurationForm = getDummyReplConfigurationForm();
    JPanel jpanelForm = new JPanel();
    jpanelForm.add(replConfigurationForm);
    ReplConfigurationDialog replConfigurationDialog = new ReplConfigurationDialog(
        getDummyReplConfigurationForm());

    //  when
    replConfigurationDialog.replaceReplConfigurationFormWithIn(replConfigurationForm, jpanelForm);

    //  then
    assertSame("In the dialog configuration form gets properly replaced in the panel ",
        replConfigurationForm.getContentPane(), jpanelForm.getComponent(0));
  }

  @Test
  public void testReplaceReplConfigurationFormWithInEmptyJPanelFormWorks() {
    //  given
    ReplConfigurationForm replConfigurationForm = getDummyReplConfigurationForm();
    JPanel jpanelForm = new JPanel();
    ReplConfigurationDialog replConfigurationDialog = new ReplConfigurationDialog(
        getDummyReplConfigurationForm());

    //  when
    replConfigurationDialog.replaceReplConfigurationFormWithIn(replConfigurationForm, jpanelForm);

    //  then
    assertSame("In the dialog configuration form gets properly added to the panel",
        replConfigurationForm.getContentPane(), jpanelForm.getComponent(0));
  }

  @Test
  public void testOnOk() {
    //  given
    ReplConfigurationForm replConfigurationForm = getDummyReplConfigurationForm();
    ReplConfigurationForm spyForm = spy(replConfigurationForm);
    ReplConfigurationDialog dialog = new ReplConfigurationDialog(spyForm);

    //  when
    dialog.onOk();

    //  then
    verify(spyForm, times(1)).updateModel();
  }
}