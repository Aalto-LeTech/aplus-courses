package fi.aalto.cs.apluscourses.intellij.model.tutorial.component;

import com.intellij.ide.navigationToolbar.NavBarRootPaneExtension;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeRootPaneNorthExtension;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeRootPane;
import com.intellij.openapi.wm.impl.WindowManagerImpl;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.util.ActionUtil;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.utils.Cast;
import java.awt.Component;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJBuildButton extends IntelliJTutorialComponent<Component> {

  public IntelliJBuildButton(@Nullable TutorialComponent parent, @Nullable Project project) {
    super(parent, project);
  }

  @Override
  protected @Nullable Component getAwtComponent() {
    var windowManager = (WindowManagerImpl) WindowManager.getInstance();
    return Optional.ofNullable(getProject())
        .map(windowManager::getProjectFrameRootPane)
        .map(IntelliJBuildButton::findNavBar)
        .map(Cast.to(NavBarRootPaneExtension.class)::orNull)
        .map(NavBarRootPaneExtension::getComponent)
        .map(IntelliJBuildButton::findBuildButton)
        .orElse(null);
  }

  private static IdeRootPaneNorthExtension findNavBar(@NotNull IdeRootPane ideRootPane) {
    return ideRootPane.findByName(IdeStatusBarImpl.NAVBAR_WIDGET_KEY);
  }

  private static @Nullable ActionButton findBuildButton(@NotNull Component root) {
    return ActionUtil.findActionButton("CompileDirty", root);
  }

}
