package fi.aalto.cs.apluscourses.intellij.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.services.MainViewModelProvider;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.Module;
import fi.aalto.cs.apluscourses.model.ModuleInstaller;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ListSelectionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ImportModuleActionTest {

  private Project project;
  private MainViewModel mainViewModel;
  private ModuleInstaller moduleInstaller;

  /**
   * Called before each test method call.  Initializes private fields.
   * @throws MalformedURLException Shouldn't happen.
   */
  @Before
  public void createMockObjects() throws MalformedURLException  {
    project = mock(Project.class);

    mainViewModel = new MainViewModel();
    MainViewModelProvider mainViewModelProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainViewModelProvider).getMainViewModel(project);

    List<Module> modules = new ArrayList<>();
    modules.add(new Module("module1", new URL("https://example.com/1")));
    modules.add(new Module("module2", new URL("https://example.com/2")));
    modules.add(new Module("module3", new URL("https://example.com/3")));

    Course course = new Course("course", modules, Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    moduleInstaller = mock(ModuleInstaller.class);
  }

  @SuppressWarnings({"ConstantConditions"})
  @Test
  public void testUpdate() {
    ImportModuleAction action = new ImportModuleAction(p -> mainViewModel, c -> moduleInstaller);

    Presentation presentation = new Presentation();
    AnActionEvent e = mock(AnActionEvent.class);
    doReturn(presentation).when(e).getPresentation();
    doReturn(project).when(e).getProject();

    action.update(e);
    assertFalse(presentation.isEnabledAndVisible());

    ListSelectionModel selectionModel = mainViewModel.courseViewModel.get()
        .getModules()
        .getSelectionModel();

    selectionModel.addSelectionInterval(1, 2);
    action.update(e);
    assertTrue(presentation.isEnabledAndVisible());

    selectionModel.clearSelection();
    action.update(e);
    assertFalse(presentation.isEnabledAndVisible());
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  @Test
  public void testActionPerformed() {
    ImportModuleAction action = new ImportModuleAction(p -> mainViewModel, c -> moduleInstaller);

    AnActionEvent e = mock(AnActionEvent.class);

    ListSelectionModel selectionModel = mainViewModel.courseViewModel.get()
        .getModules()
        .getSelectionModel();

    selectionModel.addSelectionInterval(1, 2);

    action.actionPerformed(e);

    ArgumentCaptor<List<Module>> captor = ArgumentCaptor.forClass(List.class);

    verify(moduleInstaller).installAsync(captor.capture());

    List<Module> modules = mainViewModel.courseViewModel.get().getModel().getModules();

    assertEquals("installAsync() should be called with list of size 2.",
        2, captor.getValue().size());
    assertTrue("The second module should get installed.",
        captor.getValue().contains(modules.get(1)));
    assertTrue("The third module should get installed.",
        captor.getValue().contains(modules.get(2)));
  }
}
