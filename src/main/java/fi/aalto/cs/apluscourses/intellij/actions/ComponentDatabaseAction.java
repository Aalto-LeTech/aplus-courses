package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.ui.ideactivities.ComponentDatabase;
import fi.aalto.cs.apluscourses.ui.ideactivities.EditorHighlighter;
import fi.aalto.cs.apluscourses.ui.ideactivities.OverlayPane;
import org.jetbrains.annotations.NotNull;

/**
 * Please ignore this class in any potential code reviews.
 * This class is only used for testing purposes, and will be removed in production.
 */
public class ComponentDatabaseAction extends DumbAwareAction {

  private OverlayPane overlay;

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    if (overlay == null) {
      overlay = OverlayPane.installOverlay();
    }

    var goodStuffEditor = ComponentDatabase.getEditorWindow("GoodStuff");
    var catDisplayEditor = ComponentDatabase.getEditorWindow("CategoryDisplay");
    if (goodStuffEditor == null || catDisplayEditor == null) {
      Messages.showErrorDialog(
          "Please have GoodStuff.scala and CategoryDisplayWindow.scala"
          + " open in separate editor windows.", "Null Check Failed");
      return;
    }

    var goodStuff = new EditorHighlighter(goodStuffEditor);
    goodStuff.highlightAllLines();
    overlay.addHighlighter(goodStuff);

    var catDisplay = new EditorHighlighter(catDisplayEditor);
    catDisplay.highlightLines(1, 2, 3, 5, 7, 8, 9, 55, 56, 88);
    overlay.addHighlighter(catDisplay);
  }
}
