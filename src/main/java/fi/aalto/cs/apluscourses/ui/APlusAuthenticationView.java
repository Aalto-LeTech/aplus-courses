package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.labels.LinkLabel;
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
    setTitle(getText("ui.authenticationView.name"));
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
      return new ValidationInfo(getText("ui.authenticationView.noEmptyToken"),
          inputField);
    }
    return null;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return inputField;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() { //
    tokenPageLink = new LinkLabel<>(
        getText("ui.authenticationView.tokenLink"),
        AllIcons.Ide.External_link_arrow,
        (first, second) ->
            BrowserUtil.browse(authenticationViewModel.getAuthenticationHtmlUrl()));
    tokenPageLink.setIconTextGap(0);
    tokenPageLink.setHorizontalTextPosition(SwingConstants.LEFT);
  }
}
