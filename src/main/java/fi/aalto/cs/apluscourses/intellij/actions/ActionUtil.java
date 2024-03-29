package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import java.awt.Component;
import java.awt.event.ActionListener;
import org.jetbrains.annotations.NotNull;

public class ActionUtil {

  /**
   * Launches {@link AnAction}.
   *
   * @param actionId    Id of the action.
   * @param dataContext A {@link DataContext} for the action.
   */
  public static void launch(@NotNull String actionId, @NotNull DataContext dataContext) {
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206130119/comments/206169635
    AnAction action = ActionManager.getInstance().getAction(actionId);
    AnActionEvent event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN,
        dataContext);
    action.actionPerformed(event);
  }

  /**
   * Returns an {@link ActionListener} that launches {@link AnAction} corresponding to the given
   * id each time {@code actionPerformed()} is called.
   *
   * @param actionId Id of the action.
   * @param source   A {@link Component} that gives the {@link DataContext} for the action.
   * @return An {@link ActionListener}
   */
  public static ActionListener createOnEventLauncher(@NotNull String actionId,
                                                     @NotNull Component source) {
    return actionEvent -> launch(actionId, DataManager.getInstance().getDataContext(source));
  }

  private ActionUtil() {

  }
}
