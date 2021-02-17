package fi.aalto.cs.apluscourses.model;

import com.intellij.ide.BrowserUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class UrlRenderer {

  public void show(@NotNull String url) throws Exception {
    BrowserUtil.browse(new URI(url));
  }

}
