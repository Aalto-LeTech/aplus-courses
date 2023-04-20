package fi.aalto.cs.apluscourses.presentation.base;

import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;

public class Message {
  private final @NotNull String groupId;
  private final @NotNull String title;
  private final @NotNull String content;
  private final @NotNull Level level;

  public Message(@NotNull String groupId, @NotNull String title, @NotNull String content, @NotNull Level level) {
    this.groupId = groupId;
    this.title = title;
    this.content = content;
    this.level = level;
  }

  public @NotNull String getGroupId() {
    return groupId;
  }

  public @NotNull String getTitle() {
    return title;
  }

  public @NotNull String getContent() {
    return content;
  }

  public @NotNull Level getLevel() {
    return level;
  }
}
