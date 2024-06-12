package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.labels.LinkLabel;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class APlusAuthenticationView extends DialogWrapper implements Dialog {

  private static final Logger logger = APlusLogger.logger;

  @GuiObject
  protected JPasswordField inputField;
  private JPanel basePanel;
  @GuiObject
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
    init();
  }

  private void enableDialog(boolean isEnabled) {
    inputField.setEnabled(isEnabled);
    setOKActionEnabled(isEnabled);
    if (isEnabled) {
      inputField.requestFocusInWindow();
    }
  }

  private void showValidationResult(@Nullable ValidationInfo result) {
    updateErrorInfo(result == null ? List.of() : List.of(result));
  }

  private void checkToken() {
    logger.debug("Validating token");

    if (inputField.getPassword().length == 0) {
      enableDialog(true);
      showValidationResult(new ValidationInfo(getText("ui.authenticationView.noEmptyToken"),
          inputField).withOKEnabled());
      return;
    }

    var input = inputField.getPassword();
    authenticationViewModel.setToken(input);
    authenticationViewModel.build();
    Arrays.fill(input, '\0');

    Executors.newSingleThreadExecutor().submit(() -> {
      ValidationInfo lastValidationResult = null;
      try {
        authenticationViewModel.tryGetUser(authenticationViewModel.getAuthentication());
      } catch (UnexpectedResponseException e) {
        logger.warn("Invalid token while authenticating", e);
        lastValidationResult = new ValidationInfo(getText("ui.authenticationView.invalidToken"),
            inputField).withOKEnabled();
      } catch (IOException e) {
        logger.warn("Connection error while authenticating", e);
        lastValidationResult = new ValidationInfo(getText("ui.authenticationView.connectionError"),
            inputField).withOKEnabled();
      } finally {
        final ValidationInfo finalLastValidationResult = lastValidationResult;
        ApplicationManager.getApplication().invokeLater(() -> {
          enableDialog(true);
          showValidationResult(finalLastValidationResult);
          if (finalLastValidationResult == null) {
            close(OK_EXIT_CODE);
          }
        }, ModalityState.any());
      }
    });
  }

  @Override
  protected void doOKAction() {
    enableDialog(false);
    checkToken();
  }

  @NotNull
  @Override
  protected Action @NotNull [] createActions() {
    return new Action[] {getOKAction(), getCancelAction()};
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @Override
  protected boolean continuousValidation() {
    return false;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return inputField;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    tokenPageLink = new LinkLabel<>(
        getText("ui.authenticationView.tokenLink"),
        AllIcons.Ide.External_link_arrow,
        (first, second) ->
            BrowserUtil.browse(authenticationViewModel.getAuthenticationHtmlUrl()));
    tokenPageLink.setIconTextGap(0);
    tokenPageLink.setHorizontalTextPosition(SwingConstants.LEFT);
  }
}
