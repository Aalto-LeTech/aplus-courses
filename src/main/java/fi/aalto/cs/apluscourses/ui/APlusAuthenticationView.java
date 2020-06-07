package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import fi.aalto.cs.apluscourses.model.APlusAuthentication;
import fi.aalto.cs.apluscourses.presentation.APlusAuthenticationViewModel;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthenticationView extends DialogWrapper {
  @Binding
  protected JPasswordField inputField;
  private JPanel basePanel;

  APlusAuthenticationViewModel authenticationViewModel;

  /**
   * Construct an instance with the given authentication view model.
   */
  public APlusAuthenticationView(@NotNull APlusAuthenticationViewModel authenticationViewModel) {
    super(authenticationViewModel.getProject());
    this.authenticationViewModel = authenticationViewModel;
    setTitle("A+ Token");
    setButtonsAlignment(SwingConstants.CENTER);
    init();
  }

  /**
   * If the given authentication view model already contains an authentication, then that is
   * returned. Otherwise the user is prompted for authentication, which is then set to the given
   * view model and returned. The user may cancel the authentication prompt, in which case this
   * method returns {@code null}. This method is useful for prompting the user for authentication in
   * an on-demand fashion. Since the authentication prompt can be cancelled, all actions using this
   * method should be prepared for the user cancelling the action.
   */
  @Nullable
  public static APlusAuthentication promptForAuthenticationIfMissing(
      @NotNull APlusAuthenticationViewModel authenticationViewModel) {
    if (authenticationViewModel.getAuthentication() != null) {
      return authenticationViewModel.getAuthentication();
    }
    new APlusAuthenticationView(authenticationViewModel).show();
    return authenticationViewModel.getAuthentication();
  }

  @Override
  protected void doOKAction() {
    char[] input = inputField.getPassword();
    authenticationViewModel.setToken(input);
    Arrays.fill(input, '\0');
    super.doOKAction();
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    if (inputField.getPassword().length != 0) {
      return null;
    }
    return new ValidationInfo("Token must not be empty", inputField);
  }

}
