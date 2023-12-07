package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.CoursesClient;
import org.jetbrains.annotations.NotNull;

public interface Authentication extends CoursesClient.HttpAuthentication {

  void persist(@NotNull Runnable onSuccess, @NotNull Runnable onFailure);

  void clear();
}
