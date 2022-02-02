package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import fi.aalto.cs.apluscourses.presentation.CourseItemViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseSelectionViewModel;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import fi.aalto.cs.apluscourses.ui.base.SingleSelectionList;
import fi.aalto.cs.apluscourses.ui.base.TextField;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import icons.PluginIcons;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseSelectionView extends OurDialogWrapper {

  private final CourseSelectionViewModel viewModel;

  private JPanel basePanel;
  private JLabel urlFieldLabel;
  private JLabel courseListLabel;
  private SingleSelectionList<CourseItemViewModel> courseList;
  private TextField urlField;

  private static final SimpleTextAttributes NAME_TEXT_ATTRIBUTES
      = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null);
  private static final SimpleTextAttributes SEMESTER_TEXT_ATTRIBUTES
      = new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null);

  /**
   * Construct a course selection dialog.
   */
  public CourseSelectionView(@NotNull Project project,
                             @NotNull CourseSelectionViewModel viewModel) {
    super(project);
    this.viewModel = viewModel;
    setTitle(PluginResourceBundle.getText("ui.courseProject.courseSelection.title"));

    courseList.setCellRenderer(new ColoredListCellRenderer<>() {

      @Override
      protected void customizeCellRenderer(@NotNull JList<? extends CourseItemViewModel> list,
                                           CourseItemViewModel value,
                                           int index,
                                           boolean selected,
                                           boolean hasFocus) {
        setIcon(PluginIcons.A_PLUS_EXERCISE_GROUP);
        append(value.getName(), NAME_TEXT_ATTRIBUTES);
        append(" " + value.getSemester(), SEMESTER_TEXT_ATTRIBUTES);
      }
    });

    courseList.setPaintBusy(true);
    viewModel.courses.addSimpleObserver(this, a -> courseList.setPaintBusy(false));

    courseListLabel.setText(
        PluginResourceBundle.getText("ui.courseProject.courseSelection.selection"));
    String fontName = courseListLabel.getFont().getFontName();
    courseListLabel.setFont(new Font(fontName, Font.BOLD, 20));

    urlFieldLabel.setText(
        PluginResourceBundle.getText("ui.courseProject.courseSelection.textField"));
    urlField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent focusEvent) {
        courseList.clearSelection();
      }

      @Override
      public void focusLost(FocusEvent focusEvent) {
        //do nothing
      }
    });
    urlField.textBindable.bindToSource(viewModel.selectedCourseUrl);

    registerValidationItem(urlField.textBindable);

    init();
  }

  @Override
  protected @NotNull Action @NotNull [] createActions() {
    return new Action[] {getOKAction(), getCancelAction()};
  }

  @Override
  protected void doOKAction() {
    urlField.textBindable.updateSource();
    super.doOKAction();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return basePanel;
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    courseList = new SingleSelectionList<>(this::doOKAction);
    courseList.listDataBindable.bindToSource(viewModel.courses);
    courseList.selectionBindable.bindToSource(viewModel.selectedCourse);
    urlField = new TextField();
  }
}
