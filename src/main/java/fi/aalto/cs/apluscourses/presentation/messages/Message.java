package fi.aalto.cs.apluscourses.presentation.messages;

import org.jetbrains.annotations.NotNull;

public interface Message {
  @NotNull
  String getContent();

  @NotNull
  String getTitle();

  @NotNull
  Level getLevel();

  enum Level {
    INFO,
    WARN,
    ERR
  }
}
