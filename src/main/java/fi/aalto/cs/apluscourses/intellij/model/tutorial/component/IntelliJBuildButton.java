package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.ide.navigationToolbar.NavBarPanel;
import com.intellij.ide.navigationToolbar.NavBarRootPaneExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeRootPaneNorthExtension;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeRootPane;
import com.intellij.openapi.wm.impl.WindowManagerImpl;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import fi.aalto.cs.apluscourses.utils.Cast;
import java.awt.Component;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJBuildButton extends IntelliJTutorialComponent<Component> {

  protected IntelliJBuildButton(@Nullable Project project) {
    super(project);
  }

  @Override
  protected @Nullable Component getAwtComponent() {
    var windowManager = (WindowManagerImpl) WindowManager.getInstance();
    return Optional.ofNullable(getProject())
        .map(windowManager::getProjectFrameRootPane)
        .map(IntelliJBuildButton::findNavBar)
        .map(Cast.to(NavBarRootPaneExtension.class)::orNull)
        .map(NavBarRootPaneExtension::getComponent)
        .map(Cast.to(NavBarPanel.class)::orNull)
        .orElse(null);
  }

  private static IdeRootPaneNorthExtension findNavBar(@NotNull IdeRootPane ideRootPane) {
    return ideRootPane.findByName(IdeStatusBarImpl.NAVBAR_WIDGET_KEY);
  }
}
