package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;


public class OpenFileListener implements FileEditorManagerListener, ActivitiesListener {
  private final ListenerCallback whenDone;
  private final @NotNull String filePath;
  private final Project project;
  private MessageBusConnection messageBusConnection;

  public OpenFileListener(@NotNull ListenerCallback whenDone, @NotNull String filePath, @NotNull Project project) {
    this.whenDone = whenDone;
    this.filePath = filePath;
    this.project = project;
  }

  @RequiresReadLock
  @Override
  public boolean registerListener() {
    boolean alreadyOpen = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors())
        .map(FileEditor::getFile)
        .filter(Objects::nonNull)
        .anyMatch(this::checkFile);
    if (alreadyOpen) {
      return true;
    }
    messageBusConnection = project.getMessageBus().connect();
    messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    return false;
  }

  @RequiresReadLock
  @Override
  public void unregisterListener() {
    if (messageBusConnection != null) {
      messageBusConnection.disconnect();
      messageBusConnection = null;
    }
  }

  @RequiresReadLock
  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    if (checkFile(file)) {
      whenDone.callback();
    }
  }

  private boolean checkFile(@NotNull VirtualFile file) {
    return file.getPath().endsWith(filePath);
  }
}

