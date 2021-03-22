package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.refactoring.listeners.RefactoringEventData;
import com.intellij.refactoring.listeners.RefactoringEventListener;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.messages.MessageBusConnection;
import fi.aalto.cs.apluscourses.intellij.model.APlusProject;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PocAction extends DumbAwareAction {

  @NotNull
  private APlusProject project;

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    project = new APlusProject(e.getProject());
    //explore what events can be listened to and register related listeners

    //1 - Listen for file save event (and 2 - access contents)
    //3 - Listen for file rename event
    FileListener fileListener = new FileListener();
    fileListener.registerListeners();

    // 4 - Existing IntelliJ's buttons clicked e.g. debug, run
    APlusActionListener myAnActionListener = new APlusActionListener();
    myAnActionListener.registerListener();

    // 5 - File opened! Also, get the file's path.
    OpenListener openListener = new OpenListener("/GoodStuff/o1/goodstuff/Category.scala");
    openListener.registerListeners();

    //Refactoring element listener
    RefactoringListener refactoringListener = new RefactoringListener();
    refactoringListener.registerListeners();
  }

  private class APlusActionListener implements AnActionListener {

    public void registerListener() {
      MessageBusConnection messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(AnActionListener.TOPIC, this);
    }

    @Override
    public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext,
                                      @NotNull AnActionEvent event) {
      System.out.println("Action: " + action.getTemplateText()
              + " ActionEvent: " + event.getPlace());
    }
  }

  private class RefactoringListener implements RefactoringEventListener {
    @RequiresReadLock
    public synchronized void registerListeners() {
      MessageBusConnection messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(RefactoringEventListener.REFACTORING_EVENT_TOPIC, this);
    }

    @Override
    public void refactoringStarted(@NotNull String refactoringId,
                                   @Nullable RefactoringEventData beforeData) {

      if (beforeData != null) {
        System.out.println("beforeData " + beforeData.getUserDataString());
      }
      //get the old name from this to make sure the user is renaming the correct element!
    }

    @Override
    public void refactoringDone(@NotNull String refactoringId,
                                @Nullable RefactoringEventData afterData) {
      System.out.println("refactoringId " + refactoringId);
      if (afterData != null) {
        System.out.println("afterData " + afterData.getUserDataString());
      }
      //{element=PsiField:stylus} the new name can be read from this String
      //also it can be recognized if the rename is done on PsiField, PsiMethod or PsiClass
    }

    @Override
    public void conflictsDetected(@NotNull String refactoringId,
                                  @NotNull RefactoringEventData conflictsData) {
      //not implemented
    }

    @Override
    public void undoRefactoring(@NotNull String refactoringId) {
      //not implemented
    }
  }

  private class FileListener implements BulkFileListener {

    @RequiresReadLock
    public synchronized void registerListeners() {
      MessageBusConnection messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
      for (VFileEvent event : events) {
        VirtualFile eventFile = event.getFile();
        boolean inProject = ProjectRootManager.getInstance(project.getProject())
                .getFileIndex().isInContent(eventFile);
        if (inProject) {
          if (event instanceof VFileContentChangeEvent) {
            System.out.println(eventFile.getPath() + " saved: " + event.isFromSave());
            //when pressing ctrl+s and if there are changes in the files.
            //The file's contents can be accessed by the following commented out code:
            /*try {
              System.out.println(VfsUtil.loadText(eventFile));
            } catch (IOException e) {
              e.printStackTrace();
            }*/
          } else if (event instanceof VFilePropertyChangeEvent
              && VirtualFile.PROP_NAME.equals(
                      ((VFilePropertyChangeEvent)event).getPropertyName())) {
            System.out.println("Rename from: " +  ((VFilePropertyChangeEvent) event).getOldPath()
                    + "to: " + ((VFilePropertyChangeEvent) event).getNewPath());
          }
        }
      }
    }
  }

  private class OpenListener implements FileEditorManagerListener {
    private String filePath;

    public OpenListener(String filePath) {
      this.filePath = filePath;
    }

    @RequiresReadLock
    public synchronized void registerListeners() {
      FileEditor[] editors = FileEditorManager.getInstance(project.getProject()).getAllEditors();
      for (FileEditor editor : editors) {
        if (editor.getFile().getPath().endsWith(filePath)) {
          System.out.println("File is already open");
        }
      }
      MessageBusConnection messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
      if (file.getPath().endsWith(filePath)) {
        System.out.println("File was just opened");
      }
    }
  }
}

