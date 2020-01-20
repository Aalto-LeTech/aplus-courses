package fi.aalto.cs.intellij.activities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.intellij.common.BuildInfo;
import org.jetbrains.annotations.NotNull;

public class VersionCheckerActivity implements StartupActivity {
  @Override
  public void runActivity(@NotNull Project project) {
    if (BuildInfo.INSTANCE.version.major == 0) {

    }
  }
}
