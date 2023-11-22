package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.sbt.project.settings.SbtProjectSettings;

public class AboutAction extends DumbAwareAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (e.getProject() == null) {
      return;
    }

    var settings = new SbtProjectSettings();
    settings.setupNewProjectDefault();
    settings.setExternalProjectPath(ExternalSystemApiUtil.normalizePath("C:\\Users\\Rekishi\\Desktop\\programming2\\A1120-scala3-r03-combinational"));
    var x = ExternalSystemApiUtil.getManager(ProjectSystemId.findById("SBT"));
    AbstractExternalSystemSettings<?, SbtProjectSettings, ?> extSettings = (AbstractExternalSystemSettings<?, SbtProjectSettings, ?>) x.getSettingsProvider().fun(e.getProject());
    extSettings.linkProject(settings);

    ActionUtil.launch("ExternalSystem.RefreshAllProjects", e.getDataContext());

    //new AboutDialog(e.getProject()).show();
  }
}
