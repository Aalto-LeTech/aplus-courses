package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.execution.configurations.RefactoringListenerProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringListenerManager;
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

    //1 - Listen for file save event
    //2 - Listen for file rename event
    FileListener fileListener = new FileListener();
    fileListener.registerListeners();

    // 3 - Access the saved file's contents
    // VirtualFile -> can load its text as a String.
    // Utilizing some system similar to GitHub's merges we could isolate a specific area and check.
    // VfsUtilCore.loadText(eventFile); -> IOException

    // 4 - Existing IntelliJ's buttons clicked e.g. debug, run

    // 5 - File opened! Also, get the file's path.
    OpenListener openListener = new OpenListener("/GoodStuff/o1/goodstuff/Category.scala");
    openListener.registerListeners();

    // 6 - Variable renamed RefactoringElementListener
    //RefactoringListenerManager.getInstance(project.getProject()).addListenerProvider(
      //      (new MyRefactoringListenerProvider()));

  }

  private class FileListener implements BulkFileListener {
    //private final Logger logger = LoggerFactory.getLogger(SaveListener.class);

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
            //or event.isFromSave()
            System.out.println(eventFile.getPath() + " is from save: " + event.isFromSave());
            //when pressing ctrl+s and if there are changes in the files. Perhaps could also filter which files the changes were?

          } else if (event instanceof VFilePropertyChangeEvent
              && VirtualFile.PROP_NAME.equals(((VFilePropertyChangeEvent)event).getPropertyName())) {
            System.out.println("Rename from: " +  ((VFilePropertyChangeEvent) event).getOldPath() + "to: " + ((VFilePropertyChangeEvent) event).getNewPath());
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
      for (FileEditor editor : editors) { //turn to lambda?
        if (editor.getFile().getPath().endsWith(filePath)) {
          System.out.println("File is already open"); //works
        }
      }
      MessageBusConnection messageBusConnection = project.getMessageBus().connect();
      messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
      if (file.getPath().endsWith(filePath)) {
        System.out.println("File was just opened"); //works
      }
    }
  }
}

