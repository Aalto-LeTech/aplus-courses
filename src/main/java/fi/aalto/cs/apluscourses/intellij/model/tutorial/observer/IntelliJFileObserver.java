package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.IntelliJTutorialClientObject;
import fi.aalto.cs.apluscourses.intellij.utils.VfsUtil;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.nio.file.Path;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class IntelliJFileObserver extends IntelliJMessageBusObserverBase<FileEditorManagerListener>
    implements IntelliJTutorialClientObject {
  private final @NotNull Path pathSuffix;
  private final @NotNull FileEditorManagerListener listener;

  public IntelliJFileObserver(@NotNull String action,
                              @NotNull String pathSuffix,
                              @NotNull TutorialComponent component) {
    super(FileEditorManagerListener.FILE_EDITOR_MANAGER, component);
    this.pathSuffix = Path.of(pathSuffix);
    listener = chooseListener(action);
  }

  @Override
  protected @NotNull FileEditorManagerListener getMessageListener() {
    return listener;
  }

  protected @NotNull FileEditorManagerListener chooseListener(@NotNull String action) {
    switch (action) {
      case FILE_OPEN:
        return new FileOpenedListener();
      case FILE_CLOSE:
        return new FileClosedListener();
      default:
        throw new IllegalArgumentException("Unknown file action: " + action);
    }
  }

  private void onAction(@NotNull VirtualFile file) {
    if (checkFile(file)) {
      fire();
    }
  }

  private boolean checkFile(@NotNull VirtualFile file) {
    return Optional.ofNullable(VfsUtil.getNioPath(file)).filter(this::checkPath).isPresent();
  }

  private boolean checkPath(@NotNull Path path) {
    return path.endsWith(pathSuffix);
  }

  private class FileOpenedListener implements FileEditorManagerListener {
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
      onAction(file);
    }
  }

  private class FileClosedListener implements FileEditorManagerListener {
    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
      onAction(file);
    }
  }
}
