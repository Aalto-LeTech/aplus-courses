package fi.aalto.cs.apluscourses.ui;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationViewTest extends LightIdeaTestCase {

  class TestAuthenticationView extends APlusAuthenticationView {

    public TestAuthenticationView() {
      super(new AuthenticationViewModel(new APlusAuthentication(10)), mock(Project.class));
    }

    public TestAuthenticationView(AuthenticationViewModel viewModel) {
      super(viewModel, mock(Project.class));
    }

    public void setInput(@NotNull String text) {
      super.inputField.setText(text);
    }

  }

  @Test
  public void testAplusAuthenticationView() {
    TestAuthenticationView authenticationView = new TestAuthenticationView();

    Assert.assertEquals("The dialog has 'OK' and 'Cancel' buttons",
        2, authenticationView.createActions().length);
    Assert.assertThat("The dialog title mentions 'A+ Token'", authenticationView.getTitle(),
        containsString("A+ Token"));
  }

  @Test
  public void testSetsViewModelAfterOk() {
    Authentication authentication = new APlusAuthentication(20);
    AuthenticationViewModel authenticationViewModel = new AuthenticationViewModel(authentication);
    TestAuthenticationView authenticationView = new TestAuthenticationView(authenticationViewModel);

    Assert.assertFalse(authentication.isSet());

    authenticationView.setInput("token");
    authenticationView.doOKAction();

    Assert.assertTrue("The authentication dialog modifies the model", authentication.isSet());
  }

  @Test
  public void testDoesNotSetViewModelAfterCancel() {
    Authentication authentication = new APlusAuthentication(20);
    AuthenticationViewModel authenticationViewModel = new AuthenticationViewModel(authentication);
    TestAuthenticationView authenticationView = new TestAuthenticationView(authenticationViewModel);

    Assert.assertFalse(authentication.isSet());

    authenticationView.setInput("another token");
    authenticationView.doCancelAction();

    Assert.assertFalse("The authentication dialog does not modify the model after cancelling",
        authentication.isSet());
  }

  @Test
  public void testValidation() {
    TestAuthenticationView authenticationView = new TestAuthenticationView();

    Assert.assertNotNull("The dialog does not accept an empty input field",
        authenticationView.doValidate());
    Assert.assertThat(authenticationView.doValidate().message,
        containsString("must not be empty"));

    authenticationView.setInput("not empty");
    Assert.assertNull("The dialog accepts a nonempty input field",
        authenticationView.doValidate());
  }

}
