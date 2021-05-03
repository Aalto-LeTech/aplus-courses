package fi.aalto.cs.apluscourses.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.OpenFileListener;

public class OpenFileTask extends Task {

  private final String file;
  private OpenFileListener listener;

  public OpenFileTask(String file) {
    super("editorOpen");
    this.file = file;
  }

  public String getFile() {
    return file;
  }

  @Override
  public synchronized boolean startTask(Project project) {
    listener = new OpenFileListener(this);
    listener.registerListener(project);
    return listener.isAlreadyComplete();
  }

  @Override
  public synchronized void endTask() {
    this.listener.unregisterListener();
  }

}
