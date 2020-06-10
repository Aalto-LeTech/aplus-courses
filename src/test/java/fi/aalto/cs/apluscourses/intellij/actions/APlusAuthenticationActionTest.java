package fi.aalto.cs.apluscourses.intellij.actions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.junit.Test;

public class APlusAuthenticationActionTest {

  @Test(expected = IllegalStateException.class)
  public void testShowsDialog() {
    // This is a bit of a silly test, but the token dialog uses IntelliJ's DialogWrapper, which
    // throws an IllegalStateException if the dialog is shown outside of the the IDEA UI thread.
    AnActionEvent actionEvent = mock(AnActionEvent.class);
    doReturn(null).when(actionEvent).getProject();
    new APlusAuthenticationAction().actionPerformed(actionEvent);
  }

}
