package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Authentication;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Interfaces {
  @FunctionalInterface
  public interface AuthenticationProvider {
    Authentication getAuthentication(@Nullable Project project);
  }

  @FunctionalInterface
  public interface Tagger {
    void putSystemLabel(@Nullable Project project, @NotNull String tag, int color);
  }

  @FunctionalInterface
  public interface DocumentSaver {
    void saveAllDocuments();
  }

  @FunctionalInterface
  public interface LanguageSource {
    @NotNull String getLanguage(@NotNull Project project);
  }

  @FunctionalInterface
  public interface AssistantModeProvider {
    boolean isAssistantMode();
  }
}
