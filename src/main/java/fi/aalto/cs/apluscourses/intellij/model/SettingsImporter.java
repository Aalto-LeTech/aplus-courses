package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SettingsImporter {

  void importIdeSettings(@NotNull Course course) throws IOException;

  @Nullable
  String lastImportedIdeSettings();

  void importProjectSettings(@NotNull Project project, @NotNull Course course) throws IOException;

}
