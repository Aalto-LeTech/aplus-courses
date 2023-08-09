package fi.aalto.cs.apluscourses.utils.callbacks;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.model.CourseProject;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.User;
import fi.aalto.cs.apluscourses.utils.APlusLogger;
import fi.aalto.cs.apluscourses.utils.Callbacks;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AddModuleWatermark {

  private static final String ENCODING_LINE = "# -*- coding: utf-8 -*-";
  private static final String ENCODING_LINE_WITH_UNICODE = "#\u200b\u200c\u200b\u200b\u200b\u200b\u200c\u200c\u200b\u200c\u200b\u200c\u200c\u200b *-* coding: utf-8 *-*";

  private AddModuleWatermark() {

  }

  private static String createWatermark(int userId) {
    return Integer.toBinaryString(userId)
        .replace('1', '\u200b')
        .replace('0', '\u200c');
  }

  private static void addWatermark(@NotNull Path path, @Nullable User user) throws IOException {
    final int userId = user != null ? user.getId() : 0;
    final String studentId = user != null ? user.getStudentId() : "---";
    final String studentName = user != null ? user.getUserName() : "---";

    final String watermark = createWatermark(userId);

    try (Stream<String> lineStream = Files.lines(path)) {
      var newLines = lineStream.map(line -> {
        if (line.equals(ENCODING_LINE) || line.equals(ENCODING_LINE_WITH_UNICODE)) {
          return "";
        } else if (line.trim().startsWith("#")) {
          return line.replaceFirst("#", "#" + watermark);
        }
        return line;
      }).toList();

      var newLinesMutable = new ArrayList<>(newLines);

      newLinesMutable.add(0, ENCODING_LINE);
      newLinesMutable.add(1, "# Nimi: " + studentName);
      newLinesMutable.add(2, "# Opiskelijanumero: " + studentId);

      Files.write(path, newLinesMutable);
    }
  }

  /**
   * Adds a watermark to a freshly downloaded module. This is used in TRAKY courses.
   * @param project The project
   * @param module The currently extracted module
   */
  public static void postDownloadModule(@NotNull Project project, @NotNull Module module) {
    final CourseProject courseProject = PluginSettings.getInstance().getCourseProject(project);
    final User user = courseProject != null ? courseProject.getUser() : null;

    try {
      try (Stream<Path> entries = Files.walk(module.getFullPath())) {
        entries.forEach(path -> {
          final File file = path.toFile();
          if (file.isFile() && file.getName().toLowerCase().endsWith(".py")) {
            try {
              addWatermark(path, user);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }
        });
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
