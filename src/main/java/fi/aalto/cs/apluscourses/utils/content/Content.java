package fi.aalto.cs.apluscourses.utils.content;

import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public interface Content {
  void copyTo(@NotNull Path destinationPath, @NotNull Project project) throws IOException;
}
