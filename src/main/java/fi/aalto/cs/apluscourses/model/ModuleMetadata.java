package fi.aalto.cs.apluscourses.model;

import java.time.ZonedDateTime;

public class ModuleMetadata {

  private String moduleId;
  private ZonedDateTime downloadedAt;

  public ModuleMetadata(String moduleId, ZonedDateTime downloadedAt) {
    this.moduleId = moduleId;
    this.downloadedAt = downloadedAt;
  }

  public String getModuleId() {
    return moduleId;
  }

  public ZonedDateTime getDownloadedAt() {
    return downloadedAt;
  }
}
