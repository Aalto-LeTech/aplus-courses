package fi.aalto.cs.apluscourses.intellij.services;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface DefaultGroupIdSetting {
  @NotNull Optional<Long> getDefaultGroupId();

  void setDefaultGroupId(long groupId);

  void clearDefaultGroupId();
}
