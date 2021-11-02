package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectViewUtil {

  private ProjectViewUtil() {

  }

  /**
   * Method that adds a new file (pattern) to the list of files not being shown in the Project UI.
   *
   * @param ignoredFileName a {@link String} name of the file to be ignored.
   * @param project         a {@link Project} to ignore the file from.
   */
  public static void ignoreFileInProjectView(@NotNull String ignoredFileName,
                                             @NotNull Project project) {
    FileTypeManager fileTypeManager = FileTypeManager.getInstance();
    WriteCommandAction.runWriteCommandAction(project, () -> fileTypeManager
        .setIgnoredFilesList(fileTypeManager.getIgnoredFilesList() + ignoredFileName + ";"));
  }
}
