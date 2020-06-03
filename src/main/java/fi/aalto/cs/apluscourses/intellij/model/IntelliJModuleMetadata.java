package fi.aalto.cs.apluscourses.intellij.model;

import java.time.ZonedDateTime;

public class IntelliJModuleMetadata {

  private String moduleId;
  private ZonedDateTime downloadedAt;

  public IntelliJModuleMetadata(String moduleId, ZonedDateTime downloadedAt) {
    this.moduleId = moduleId;
    this.downloadedAt = downloadedAt;
  }

  public String getModuleId() {
    return moduleId;
  }

  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }

  public ZonedDateTime getDownloadedAt() {
    return downloadedAt;
  }

  public void setDownloadedAt(ZonedDateTime downloadedAt) {
    this.downloadedAt = downloadedAt;
  }
}
