package fi.aalto.cs.apluscourses.model;

import java.awt.Desktop;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public class UrlRenderer {

  public void show(@NotNull String url) throws Exception {
    Desktop.getDesktop().browse(new URI(url));
  }

}
