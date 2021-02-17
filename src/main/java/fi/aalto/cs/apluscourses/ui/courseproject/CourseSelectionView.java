package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import fi.aalto.cs.apluscourses.presentation.CourseItemViewModel;
import fi.aalto.cs.apluscourses.presentation.CourseSelectionViewModel;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import fi.aalto.cs.apluscourses.ui.base.TextField;
import fi.aalto.cs.apluscourses.utils.PluginResourceBundle;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CourseSelectionView extends OurDialogWrapper {
  private CourseSelectionViewModel viewModel;

  private JPanel basePanel;
  private JLabel urlFieldLabel;
  private JLabel courseListLabel;
  private JList<CourseItemViewModel> courseList;
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

    courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    courseList.addListSelectionListener(listSelectionEvent -> {
      CourseItemViewModel selected = courseList.getSelectedValue();
      if (selected != null) {
        viewModel.selectedCourseUrl.set(selected.getUrl());
      }
    });

    courseList.setCellRenderer(new ColoredListCellRenderer<CourseItemViewModel>() {
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
      }
    });
    urlField.textBindable.bindToSource(viewModel.selectedCourseUrl);

    registerValidationItem(urlField.textBindable);

    setButtonsAlignment(SwingConstants.CENTER);

    init();
  }

  @Override
  protected @NotNull Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
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
    courseList = new JBList<>(viewModel.getCourses());
    urlField = new TextField();
  }
}
