package fi.aalto.cs.apluscourses.ui;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.testFramework.LightIdeaTestCase;
import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import javax.swing.JPasswordField;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationViewTest extends LightIdeaTestCase {

  private static class TestAuthenticationView extends APlusAuthenticationView {

    public TestAuthenticationView() {
      super(new AuthenticationViewModel(APlusTokenAuthentication::new), mock(Project.class));
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

    assertEquals("The dialog has 'OK' and 'Cancel' buttons",
        2, authenticationView.createActions().length);
    assertThat("The dialog title mentions 'A+ Token'", authenticationView.getTitle(),
        containsString("A+ Token"));
    assertTrue("The password field is the preferred focused component",
        authenticationView.getPreferredFocusedComponent() instanceof JPasswordField);
  }

  @Test
  public void testSetsViewModelAfterOk() {
    AuthenticationViewModel authenticationViewModel =
        new AuthenticationViewModel(APlusTokenAuthentication::new);
    TestAuthenticationView authenticationView = new TestAuthenticationView(authenticationViewModel);

    String tokenString = "wxyz";

    authenticationView.setInput(tokenString);
    authenticationView.doOKAction();

    Authentication authentication = authenticationViewModel.build();

    assertTrue("The authentication dialog produces the correct model object",
        ((TokenAuthentication) authentication).tokenEquals(tokenString));
  }

  @Test
  public void testValidation() {
    TestAuthenticationView authenticationView = new TestAuthenticationView();


    ValidationInfo validationInfo = authenticationView.doValidate();
    assertNotNull("The dialog does not accept an empty input field",
        validationInfo);
    assertThat(validationInfo.message,
        containsString("The token cannot be empty"));

    authenticationView.setInput("not empty");
    Assert.assertNull("The dialog accepts a nonempty input field",
        authenticationView.doValidate());
  }

}
