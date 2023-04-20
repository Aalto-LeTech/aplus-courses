package fi.aalto.cs.apluscourses.utils.dom;

import fi.aalto.cs.apluscourses.model.tutorial.Props;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface Node extends Props {

  @NotNull String getKey();

  @NotNull Stream<@NotNull Node> streamChildren();

  default @NotNull List<@NotNull Node> children() {
    return streamChildren().collect(Collectors.toList());
  }

  @NotNull String getContent();
}
