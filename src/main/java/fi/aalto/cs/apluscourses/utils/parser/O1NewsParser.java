package fi.aalto.cs.apluscourses.utils.parser;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class O1NewsParser implements NewsParser {
  @NotNull
  private final String language;

  public O1NewsParser(@NotNull String language) {
    this.language = language;
  }

  @Override
  public @NotNull String parseTitle(@NotNull Element titleElement) {
    return getElementsByLanguage(titleElement).first().text();
  }

  @Override
  public @NotNull String[] parseBody(@NotNull Element bodyElement) {
    return getElementsByLanguage(bodyElement).stream().map(Element::text).toArray(String[]::new);
  }

  private Elements getElementsByLanguage(@NotNull Element element) {
    var elements = element.getElementsByClass("only" + language);
    if (elements.isEmpty()) {
      return element.getElementsByClass("onlyen");
    }
    return elements;
  }
}
