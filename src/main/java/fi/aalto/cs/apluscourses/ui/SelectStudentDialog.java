package fi.aalto.cs.apluscourses.ui;

import static fi.aalto.cs.apluscourses.utils.PluginResourceBundle.getText;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import fi.aalto.cs.apluscourses.model.Student;
import fi.aalto.cs.apluscourses.presentation.SelectStudentViewModel;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import icons.PluginIcons;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectStudentDialog extends OurDialogWrapper {
  private final SelectStudentViewModel viewModel;
  private JPanel basePanel;
  @GuiObject
  private JList<Student> studentList;
  @GuiObject
  private JScrollPane scrollPane;

  /**
   * A constructor.
   */
  public SelectStudentDialog(@NotNull SelectStudentViewModel viewModel, @NotNull Project project) {
    super(project);
    this.viewModel = viewModel;
    setTitle(getText("ui.selectStudentDialog.title"));
    init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return basePanel;
  }

  @Override
  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return studentList;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    scrollPane = new JBScrollPane();

    studentList = new StudentList();
    studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    studentList.addListSelectionListener(listSelectionEvent -> {
      var selected = studentList.getSelectedValue();
      if (selected != null) {
        viewModel.selectedStudent.set(selected);
      }
    });

    studentList.setCellRenderer(new ColoredListCellRenderer<>() {
      @Override
      protected void customizeCellRenderer(@NotNull JList<? extends Student> list,
                                           Student value,
                                           int index,
                                           boolean selected,
                                           boolean hasFocus) {
        setIcon(PluginIcons.A_PLUS_USER_ACTIVE);
        append(value.getPresentableName());
        SpeedSearchUtil.applySpeedSearchHighlighting(list, this, true, selected);
      }
    });
  }

  private class StudentList extends JBList<Student> {
    public StudentList() {
      super(viewModel.getStudents());
      new ListSpeedSearch<>(this, Student::getPresentableName);
    }
  }
}
