package fi.aalto.cs.apluscourses.model;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.browsers.OpenInBrowserRequestKt;
import com.intellij.ide.browsers.WebBrowserService;
import com.intellij.ide.browsers.WebBrowserUrlProvider;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import java.net.URI;
import java.net.URISyntaxException;
import org.jetbrains.annotations.NotNull;

public class UrlRenderer {

  public void show(@NotNull String url) throws URISyntaxException {
    this.show(new URI(url));
  }

  public void show(@NotNull URI uri) {
    BrowserUtil.browse(uri);
  }

  /**
   * Opens the requested file in a default browser. The file is opened using
   * IntelliJ's built-in web server, not the file:// protocol.
   * @param project Project in which the VirtualFile is contained.
   * @param file The VirtualFile to open.
   */
  public void show(@NotNull Project project, @NotNull VirtualFile file) throws WebBrowserUrlProvider.BrowserException {
    ReadAction.run(() -> {
      if (project.isDisposed() || !file.isValid()) {
        return;
      }

      PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile != null) {
        var openRequest = OpenInBrowserRequestKt.createOpenInBrowserRequest(psiFile, false);
        if (openRequest != null) {
          WebBrowserService.getInstance().getUrlsToOpen(openRequest, false)
                  .stream()
                  .findFirst()
                  .ifPresent(url -> BrowserUtil.browse(url.toExternalForm()));
        }
      }
    });
  }

}
