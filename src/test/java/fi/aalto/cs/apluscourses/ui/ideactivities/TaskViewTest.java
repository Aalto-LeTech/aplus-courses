package fi.aalto.cs.apluscourses.ui.ideactivities;

import fi.aalto.cs.apluscourses.model.Task;
import fi.aalto.cs.apluscourses.presentation.ideactivities.TaskViewModel;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class TaskViewTest {

  public class TestTaskView extends TaskView {
    public TestTaskView(@NotNull TaskViewModel viewModel) {
      super(viewModel);
    }

    public String getTaskDescriptionText() {
      return label.getText();
    }
  }

  @NotNull
  private TaskViewModel createViewModel() {
    return new TaskViewModel(new Task());
  }

  @NotNull
  private TestTaskView createTestTaskView() {
    return new TestTaskView(
        createViewModel()
    );
  }

  @Test
  public void testTaskViewDescriptionText() {
    TestTaskView testTaskView = createTestTaskView();
    Assert.assertEquals("The view has the correct task info", "<html>editor.open<br>"
                + "GoodStuff/o1/goodstuff/gui/CategoryDisplayWindow.scala</html>",
        testTaskView.getTaskDescriptionText());
  }
}