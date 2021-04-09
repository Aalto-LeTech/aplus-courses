package fi.aalto.cs.apluscourses.utils;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.model.Task;
import org.jetbrains.annotations.NotNull;

public interface ActivitiesListener {
  //the Utils class approach seems better because now the OpenFileListener doesn't override anything.


  static void registerListener(Task task, Project project) {
    switch (task.getAction()) {
      case "editor.open":
        OpenFileListener openListener = new OpenFileListener(task);
        openListener.registerListeners(project);
      case "assignment_tree.submit":
         //Listener for submitting assignments?
      default:

    }
  }
/*
  private class OpenListener implements FileEditorManagerListener {
    private String filePath;
    private Project project;
    private MessageBusConnection messageBusConnection;

    public OpenListener(@NotNull Task task) {
      this.filePath = task.getFile();
    }

    @RequiresReadLock
    public synchronized void registerListeners(Project project) { //perhaps pass project in the constructor since it is a class field.
      this.project = project;
      FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
      for (FileEditor editor : editors) {
        if (editor.getFile().getPath().endsWith(filePath)) {
          System.out.println("File is already open");
        }
      }
      messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);

    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
      if (file.getPath().endsWith(filePath)) {
        System.out.println("File was just opened");
        PluginSettings.getInstance().getMainViewModel(project).tutorialViewModel.get().currentTaskCompleted();
        if (messageBusConnection != null) {
          //unregistering the listener :P
          messageBusConnection.disconnect();
          //Another alternative here would be to use Disposable. Not sure if/how we would benefit from it.
        } //put in a method of the interface as a default?/static method
      }
      //if there is no other Task, display sth like Tutorial Completed! -> handled in TutorialViewModel
    }
  }*/
}
