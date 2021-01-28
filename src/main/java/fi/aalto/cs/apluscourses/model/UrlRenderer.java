package fi.aalto.cs.apluscourses.model;

import com.intellij.ide.BrowserUtil;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public class UrlRenderer {

  public void show(@NotNull String url) throws Exception {
    BrowserUtil.browse(new URI(url));
  }

}
