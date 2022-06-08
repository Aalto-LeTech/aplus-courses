package fi.aalto.cs.apluscourses.model;

import com.intellij.ide.BrowserUtil;
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

}
