package fi.aalto.cs.apluscourses.utils.parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;

public interface NewsParser {
  @NotNull
  String parseTitle(@NotNull Element titleElement);

  @NotNull
  String[] parseBody(@NotNull Element bodyElement);
}
