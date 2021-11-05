package fi.aalto.cs.apluscourses.model.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ActivityFactory {
  @NotNull ActivitiesListener createListener(@NotNull String action,
                                             @NotNull Arguments arguments,
                                             @NotNull ListenerCallback callback);

  @NotNull ComponentPresenter createPresenter(@NotNull String component,
                                              @Nullable String instruction,
                                              @Nullable String info,
                                              @NotNull Arguments componentArguments,
                                              @NotNull Arguments actionArguments,
                                              @NotNull String @NotNull [] assertClosed,
                                              @NotNull Reaction @NotNull [] reactions,
                                              boolean isAlreadyCompleted);
}
