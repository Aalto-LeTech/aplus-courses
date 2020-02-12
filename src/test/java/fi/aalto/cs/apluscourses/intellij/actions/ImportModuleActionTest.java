package fi.aalto.cs.apluscourses.intellij.actions;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
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
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ImportModuleActionTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testImportModule() throws MalformedURLException {
    Project project = mock(Project.class);

    MainViewModel mainViewModel = new MainViewModel();
    MainViewModelProvider mainViewModelProvider = mock(MainViewModelProvider.class);
    doReturn(mainViewModel).when(mainViewModelProvider).getMainViewModel(project);

    List<Module> modules = new ArrayList<>();
    modules.add(new Module("module1", new URL("https://example.com/1")));
    modules.add(new Module("module2", new URL("https://example.com/2")));
    modules.add(new Module("module3", new URL("https://example.com/3")));

    Course course = new Course("course", modules, Collections.emptyMap());
    mainViewModel.courseViewModel.set(new CourseViewModel(course));

    ModuleInstaller.Factory moduleInstallerFactory = mock(ModuleInstaller.Factory.class);
    ModuleInstaller moduleInstaller = mock(ModuleInstaller.class);
    doReturn(moduleInstaller).when(moduleInstallerFactory).getInstallerFor(course);

    ImportModuleAction action = new ImportModuleAction(mainViewModelProvider, moduleInstallerFactory);

    Presentation presentation = new Presentation();
    AnActionEvent e = mock(AnActionEvent.class);
    doReturn(presentation).when(e).getPresentation();
    doReturn(project).when(e).getProject();

    action.update(e);

    assertFalse(presentation.isEnabledAndVisible());

    mainViewModel.courseViewModel.get().getModules().getSelectionModel().addSelectionInterval(1,2);

    action.update(e);

    assertTrue(presentation.isEnabledAndVisible());

    action.actionPerformed(e);

    ArgumentCaptor<List<Module>> captor = ArgumentCaptor.forClass(List.class);

    verify(moduleInstaller).installAsync(captor.capture());

    assertEquals("installAsync() should be called with list of size 2.",
        2, captor.getValue().size());
    assertTrue("The second module should get installed.",
        captor.getValue().contains(modules.get(1)));
    assertTrue("The third module should get installed.",
        captor.getValue().contains(modules.get(2)));
  }
}
