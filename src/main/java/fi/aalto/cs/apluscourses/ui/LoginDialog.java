package fi.aalto.cs.apluscourses.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefCookieManager;
import fi.aalto.cs.apluscourses.dal.SessionAuthentication;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.utils.Cookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefCookie;
import org.cef.network.CefCookieManager;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginDialog extends JDialog {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoginDialog.class);

  private final MainViewModel mainViewModel;
  private final SessionAuthentication sessionAuthentication = new SessionAuthentication();


  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JPanel myPanel;

  public LoginDialog(@NotNull MainViewModel mainViewModel) {
    this.mainViewModel = mainViewModel;
    setContentPane(contentPane);
    setModal(true);

    buttonOK.setEnabled(false);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    JBCefBrowser browser = new JBCefBrowser("https://plus.cs.aalto.fi/shibboleth/login/?next=");
    browser.getJBCefClient().addLoadHandler(new CefLoadHandler() {
      @Override
      public void onLoadingStateChange(CefBrowser cefBrowser, boolean b, boolean b1, boolean b2) { }

      @Override
      public void onLoadStart(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest.TransitionType transitionType) { }

      @Override
      public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
        LOGGER.debug("onLoadEnd: {}", frame.getURL());
        if (!frame.getURL().equals("https://plus.cs.aalto.fi/")) {
          return;
        }
        LOGGER.debug("Let's look at the cookies...");
        // consider using visitUrlCookies instead of visitAllCookies
        CefCookieManager.getGlobalManager().visitAllCookies((cookie, count, total, delete) -> {
          LOGGER.debug("Cookie {}/{} ({}): {}", count + 1, total, cookie.domain, cookie.name);
          sessionAuthentication.setCookie(convertCefCookie(cookie));
          if (count >= total - 1) {
            ApplicationManager.getApplication().invokeLater(() -> buttonOK.setEnabled(true));
          }
          return true;
        });
      }

      @Override
      public void onLoadError(CefBrowser cefBrowser, CefFrame cefFrame, ErrorCode errorCode, String s, String s1) { }
    }, browser.getCefBrowser());
    myPanel.add(browser.getComponent());
  }

  private void onOK() {
    mainViewModel.setAuthentication(sessionAuthentication);
    dispose();
  }

  private void onCancel() {
    dispose();
  }

  @NotNull
  private static Cookie convertCefCookie(@NotNull CefCookie cefCookie) {
    Cookie cookie = new Cookie();
    cookie.name = cefCookie.name;
    cookie.value = cefCookie.value;
    cookie.domain = cefCookie.domain;
    cookie.path = cefCookie.path;
    return cookie;
  }
}
