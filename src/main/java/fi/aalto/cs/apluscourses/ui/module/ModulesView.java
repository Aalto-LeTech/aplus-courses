package fi.aalto.cs.apluscourses.ui.module;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModulesView {
  @GuiObject
  public ModuleListView moduleListView;
  @GuiObject
  public JPanel toolbarContainer;
  @GuiObject
  private JPanel basePanel;
  private JPanel cardPanel;
  private JLabel emptyText;
  @GuiObject
  private JScrollPane pane;
  private CardLayout cl;

  /**
   * A view that holds the content of the Modules tool window.
   */
  public ModulesView() {
    // Avoid this instance getting GC'd before its UI components.
    //
    // Here we add a (strong) reference from a UI component to this object, thus ensuring that this
    // object lives at least as long as that UI component.
    //
    // This makes it possible to use this object as a weakly referred observer for changes that
    // require UI updates.
    //
    // If UI components are GC'd, this object can also go.
    //
    // It depends on the implementation of IntelliJ's GUI designer whether this "hack"
    // needed (I don't know if these objects of bound classes are strongly referred to from UI or
    // not), but it's better to play it safe.
    //
    // We use class name as a unique key for the property.
    basePanel.putClientProperty(ModulesView.class.getName(), this);
    cl = (CardLayout) cardPanel.getLayout();
    moduleListView.setBackground(new Tree().getBackground());
    emptyText.setHorizontalAlignment(SwingConstants.CENTER);
    emptyText.setVerticalAlignment(SwingConstants.CENTER);
    pane.getVerticalScrollBar().setUnitIncrement(moduleListView.getFixedCellHeight());
    emptyText.setText(getText("ui.exercise.ExercisesView.loading"));
  }

  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  /**
   * Update this modules view with the given view model (which may be null).
   */
  public void viewModelChanged(@Nullable CourseViewModel course) {
    ApplicationManager.getApplication().invokeLater(() -> {
      moduleListView.setModel(course == null ? null : course.getModules());
      cl.show(cardPanel, (course != null) ? "TreeCard" : "LabelCard");
    }, ModalityState.any()
    );
  }

  public JLabel getEmptyText() {
    return emptyText;
  }

  /**
   * Determines whether the empty text shows that the project is loading or that the user should
   * turn the project into a course project.
   */
  public void setProjectReady(boolean isReady) {
    ApplicationManager.getApplication().invokeLater(() -> emptyText.setText(isReady
        ? getText("ui.module.ModuleListView.turnIntoAPlusProject")
        : getText("ui.exercise.ExercisesView.loading")), ModalityState.any());
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    pane = ScrollPaneFactory.createScrollPane(basePanel);
  }
}
