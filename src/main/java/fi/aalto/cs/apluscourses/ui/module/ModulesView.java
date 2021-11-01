package fi.aalto.cs.apluscourses.ui.module;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.ToolbarPanel;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModulesView implements ToolbarPanel {
  @GuiObject
  public ModuleListView moduleListView;
  @GuiObject
  public JPanel toolbarContainer;
  @GuiObject
  private JPanel basePanel;
  @GuiObject
  private JScrollPane pane;

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
    pane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, JBColor.border()));
    moduleListView.setBackground(new Tree().getBackground());
    pane.getVerticalScrollBar().setUnitIncrement(moduleListView.getFixedCellHeight());
    moduleListView.setEmptyText(getText("ui.toolWindow.loading"));
  }

  @Override
  @NotNull
  public JPanel getBasePanel() {
    return basePanel;
  }

  @Override
  public @NotNull JPanel getToolbar() {
    return toolbarContainer;
  }

  @NotNull
  @Override
  public String getTitle() {
    return getText("ui.toolWindow.subTab.modules.name");
  }

  /**
   * Update this modules view with the given view model (which may be null).
   */
  public void viewModelChanged(@Nullable CourseViewModel course) {
    ApplicationManager.getApplication().invokeLater(() -> {
          moduleListView.setModel(course == null ? null : course.getModules());
          moduleListView.setEmptyText(
              course == null ? getText("ui.toolWindow.loading") : getText("ui.toolWindow.subTab.modules.noModules"));
        }, ModalityState.any()
    );
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    pane = ScrollPaneFactory.createScrollPane(basePanel);
  }
}
