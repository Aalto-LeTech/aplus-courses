package fi.aalto.cs.apluscourses.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.labels.LinkLabel;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthenticationView extends DialogWrapper implements Dialog {

  @GuiObject
  protected JPasswordField inputField;
  private JPanel basePanel;
  private LinkLabel<Object> tokenPageLink;

  AuthenticationViewModel authenticationViewModel;

  /**
   * Construct an instance with the given authentication view model.
   */
  public APlusAuthenticationView(@NotNull AuthenticationViewModel authenticationViewModel,
                                 @Nullable Project project) {
    super(project);
    this.authenticationViewModel = authenticationViewModel;
    setTitle("A+ Token");
    setButtonsAlignment(SwingConstants.CENTER);
    init();
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
    if (inputField.getPassword().length == 0) {
      return new ValidationInfo("Token must not be empty", inputField);
    }
    return null;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return inputField;
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
    tokenPageLink = new LinkLabel<>(
        "Forgot your token1?",
        AllIcons.Ide.External_link_arrow,
        (first, second) ->
            BrowserUtil.browse(PluginSettings.A_PLUS_BASE_URL + "/accounts/accounts/"));
    tokenPageLink.setIconTextGap(0);
    tokenPageLink.setHorizontalTextPosition(SwingConstants.LEFT);
  }
}
