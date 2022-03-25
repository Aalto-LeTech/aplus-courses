package fi.aalto.cs.apluscourses.utils.parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;

public class NewsParser {
  @NotNull
  public String parseTitle(@NotNull Element titleElement) {
    return titleElement.text();
  }

  @NotNull
  public String[] parseBody(@NotNull Element bodyElement) {
    return new String[] {bodyElement.text()};
  }
}
