package fi.aalto.cs.apluscourses.ui.module;

import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.ui.Binding;
import fi.aalto.cs.apluscourses.utils.ObservableProperty;
import javax.swing.JPanel;

public class ModulesView implements ObservableProperty.ValueObserver<CourseViewModel> {
  @Binding
  public ModuleListView moduleListView;
  @Binding
  public JPanel toolbarContainer;
  @Binding
  public JPanel basePanel;

  /**
   * A view that holds the content of the Modules tool window.
   */
  public ModulesView() {
    // Avoid this instance getting GC'ed before its UI components.
    //
    // Here we add a (strong) reference from a UI component to this object, thus ensuring that this
    // object lives at least as long as that UI component.
    //
    // This makes it possible to use this object as a weakly referred observer for changes that
    // require UI updates.
    //
    // If UI components are GC'ed, this object can also go.
    //
    // It depends on the implementation of IntelliJ's GUI designer whether this "hack" is actually
    // needed (I don't know if these objects of bound classes are strongly referred to from UI or
    // not), but it's better to play it safe.
    //
    // We use class name as a unique key for the property.
    basePanel.putClientProperty(ModulesView.class.getName(), this);
  }

  @Override
  public void valueChanged(CourseViewModel course) {
    moduleListView.setModel(course == null ? null : course.getModules());
  }
}
