package fi.aalto.cs.apluscourses.model.task;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.utils.ActivitiesListener;
import fi.aalto.cs.apluscourses.intellij.utils.OpenFileListener;

public class OpenFileTask extends Task {

  String file;
  OpenFileListener listener;

  public OpenFileTask(String file) {
    super("editorOpen");
    this.file = file;
  }

  public String getFile() {
    return file;
  }

  public void setListener(ActivitiesListener listener) {
    this.listener = (OpenFileListener) listener;
  }

  @Override
  public boolean registerListener(Project project) {
    this.setListener(new OpenFileListener(this));
    listener.registerListener(project);
    return listener.isAlreadyComplete();
  }

  @Override
  public ActivitiesListener getListener() {
    return this.listener;
  }

  public void setFile(String file) {
    this.file = file;
  }

}
