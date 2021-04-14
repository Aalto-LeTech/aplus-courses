package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.model.Task;
import org.jetbrains.annotations.NotNull;

public class OpenFileListener implements FileEditorManagerListener, ActivitiesListener {
  private final String filePath;
  private final Task task;
  private MessageBusConnection messageBusConnection;

  public OpenFileListener(@NotNull Task task) {
    this.task = task;
    this.filePath = task.getFile();
  }

  @RequiresReadLock
  @Override
  public void registerListener(Project project) {
    FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
    for (FileEditor editor : editors) {
      if (editor.getFile() != null && editor.getFile().getPath().endsWith(filePath)) {
        System.out.println("File is already open");
        //TODO handle this case better
        task.setIsCompleted(true);
        return;
      }
    }
    messageBusConnection = project.getMessageBus().connect();
    messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
  }

  @Override
  public void unregisterListener() {
    unregisterListener(messageBusConnection);
  }

  @RequiresReadLock
  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    if (file.getPath().endsWith(filePath)) {
      System.out.println("File was just opened");
      unregisterListener(messageBusConnection);
      task.setIsCompleted(true);
    }
  }
}

