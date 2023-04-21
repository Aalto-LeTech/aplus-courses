package fi.aalto.cs.apluscourses.intellij.model.tutorial.util;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import fi.aalto.cs.apluscourses.ui.utils.AwtUtil;
import java.awt.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionUtil {

  public static @Nullable ActionButton findActionButton(@NotNull String actionId, @NotNull Component root) {
    return findActionButton(ActionManager.getInstance().getAction(actionId), root);
  }

  public static @Nullable ActionButton findActionButton(@NotNull AnAction action, @NotNull Component root) {
    return AwtUtil.AWT_COMPOSITE.streamDescendantsOfType(ActionButton.class, root)
        .filter(actionButton -> actionButton.getAction().equals(action))
        .findAny()
        .orElse(null);
  }


}
