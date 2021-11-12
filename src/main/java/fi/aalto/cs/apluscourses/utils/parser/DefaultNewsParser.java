package fi.aalto.cs.apluscourses.utils.parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;

public class DefaultNewsParser implements NewsParser {
  @Override
  public @NotNull String parseTitle(@NotNull Element titleElement) {
    return titleElement.text();
  }

  @Override
  public @NotNull String[] parseBody(@NotNull Element bodyElement) {
    return new String[] {bodyElement.text()};
  }
}
