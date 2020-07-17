package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.CoursesClient;
import org.jetbrains.annotations.NotNull;

public interface Authentication extends CoursesClient.HttpAuthentication {

  void clear();

  int maxTokenLength();

  void setToken(@NotNull char[] newToken);

  boolean isSet();
}
