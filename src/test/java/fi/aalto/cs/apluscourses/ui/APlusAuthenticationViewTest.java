package fi.aalto.cs.apluscourses.ui;

import static org.hamcrest.CoreMatchers.containsString;

import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationViewTest extends LightIdeaTestCase {

  class TestAuthenticationView extends APlusAuthenticationView {

    public TestAuthenticationView() {
      super(new APlusAuthenticationViewModel(null));
    }

    public TestAuthenticationView(APlusAuthenticationViewModel viewModel) {
      super(viewModel);
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
    APlusAuthenticationViewModel authenticationViewModel = new APlusAuthenticationViewModel(null);
    TestAuthenticationView authenticationView
        = new TestAuthenticationView(authenticationViewModel);
    Assert.assertNull(authenticationViewModel.getAuthentication());

    authenticationView.setInput("token");
    authenticationView.doOKAction();

    Assert.assertNotNull("The authentication dialog modifies the view model",
        authenticationViewModel.getAuthentication());
  }

  @Test
  public void testDoesNotSetViewModelAfterCancel() {
    APlusAuthenticationViewModel authenticationViewModel = new APlusAuthenticationViewModel(null);
    TestAuthenticationView authenticationView
        = new TestAuthenticationView(authenticationViewModel);
    authenticationView.setInput("another token");
    authenticationView.doCancelAction();

    Assert.assertNull("The authentication dialog does not modify the view model after cancelling",
        authenticationViewModel.getAuthentication());
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
