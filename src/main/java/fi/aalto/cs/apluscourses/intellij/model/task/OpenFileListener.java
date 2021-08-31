package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.Arguments;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;


public class OpenFileListener extends ActivitiesListenerBase<@NotNull Collection<@NotNull VirtualFile>>
    implements FileEditorManagerListener {

  private final @NotNull String filePath;
  private final Project project;
  private MessageBusConnection messageBusConnection;

  /**
   * Instantiates a listener for open file events.
   *
   * @param callback Who to call when it happens?
   * @param filePath Path of the file to be opened.
   * @param project Project.
   */
  public OpenFileListener(@NotNull ListenerCallback callback,
                          @NotNull Project project,
                          @NotNull String filePath) {
    super(callback);
    this.filePath = filePath;
    this.project = project;
  }

  public static OpenFileListener create(ListenerCallback callback,
                                        Project project,
                                        Arguments arguments) {
    return new OpenFileListener(callback, project, arguments.getString("filePath"));
  }

  @RequiresEdt
  @Override
  protected void registerListenerOverride() {
    messageBusConnection = project.getMessageBus().connect();
    messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
  }

  @RequiresEdt
  @Override
  protected void unregisterListenerOverride() {
    messageBusConnection.disconnect();
    messageBusConnection = null;
  }

  @RequiresReadLock
  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    check(Collections.singleton(file));
  }

  @RequiresReadLock
  @Override
  protected boolean checkOverride(@NotNull Collection<@NotNull VirtualFile> files) {
    return files.stream().anyMatch(file -> file.getPath().endsWith(filePath));
  }

  @RequiresReadLock
  @Override
  protected @NotNull Collection<@NotNull VirtualFile> getDefaultParameter() {
    return Arrays.stream(FileEditorManager.getInstance(project).getAllEditors())
      .map(FileEditor::getFile)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
}

