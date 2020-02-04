package fi.aalto.cs.intellij.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.awt.Component;
import java.awt.event.ActionListener;

public class ActionUtil {
  public static void trigger(String actionId, Component source) {
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206130119/comments/206169635
    AnAction action = ActionManager.getInstance().getAction(actionId);
    AnActionEvent event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN,
        DataManager.getInstance().getDataContext(source));
    action.actionPerformed(event);
  }

  public static ActionListener triggerer(String actionId, Component source) {
    return actionEvent -> trigger(actionId, source);
  }

  private ActionUtil() {

  }
}
