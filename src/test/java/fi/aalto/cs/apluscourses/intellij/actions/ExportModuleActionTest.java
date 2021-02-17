package fi.aalto.cs.apluscourses.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import fi.aalto.cs.apluscourses.intellij.DialogHelper;
import fi.aalto.cs.apluscourses.intellij.model.ProjectModuleSource;
import fi.aalto.cs.apluscourses.intellij.notifications.IoErrorNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.Dialogs;
import fi.aalto.cs.apluscourses.presentation.FileSaveViewModel;
import fi.aalto.cs.apluscourses.presentation.ModuleSelectionViewModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ExportModuleActionTest {

  Project project;
  AnActionEvent event;

  ProjectModuleSource moduleSource;
  ExportModuleAction.ProjectPathResolver projectPathResolver;

  Path zipPath;
  Path modulePath;
  ExportModuleAction.DirectoryZipper zipper;

  AtomicBoolean cancelModuleSelection;
  AtomicBoolean cancelFileSave;
  DialogHelper<ModuleSelectionViewModel> moduleSelectionDialog;
  DialogHelper<FileSaveViewModel> fileSaveDialog;
  Dialogs dialogs;

  Notifier notifier;

  /**
   * Runs before every tests. Initializes mock objects and other stuff.
   */
  @Before
  public void setUp() {
    project = mock(Project.class);
    event = mock(AnActionEvent.class);
    doReturn(project).when(event).getProject();

    zipPath = Paths.get(FileUtilRt.getTempDirectory());
    modulePath = zipPath.resolve("moduleDirectory");

    Module selectedModule = mock(Module.class);
    VirtualFile moduleFile = mock(VirtualFile.class);
    VirtualFile moduleDir = mock(VirtualFile.class);
    doReturn(moduleDir).when(moduleFile).getParent();
    doReturn(modulePath).when(moduleDir).toNioPath();
    doReturn(moduleFile).when(selectedModule).getModuleFile();

    Module otherModule = mock(Module.class);
    moduleSource = mock(ProjectModuleSource.class);
    doReturn(new Module[]{otherModule, selectedModule})
        .when(moduleSource).getModules(any(Project.class));

    projectPathResolver = mock(ExportModuleAction.ProjectPathResolver.class);
    doReturn(mock(VirtualFile.class)).when(projectPathResolver).getProjectPath(any(Project.class));

    zipper = mock(ExportModuleAction.DirectoryZipper.class);

    cancelModuleSelection = new AtomicBoolean(false);
    cancelFileSave = new AtomicBoolean(false);
    moduleSelectionDialog = spy(new DialogHelper<>(
        viewModel -> {
          viewModel.selectedModule.set(viewModel.getModules()[1]);
          return !cancelModuleSelection.get();
        }
    ));
    fileSaveDialog = spy(new DialogHelper<>(
        viewModel -> {
          viewModel.setPath(zipPath);
          return !cancelFileSave.get();
        }
    ));
    Dialogs.Factory<FileSaveViewModel> fileSaveDialogFactory
        = new DialogHelper.Factory(fileSaveDialog, project);
    Dialogs.Factory<ModuleSelectionViewModel> moduleSelectionDialogFactory
        = new DialogHelper.Factory(moduleSelectionDialog, project);
    dialogs = new Dialogs();
    dialogs.register(ModuleSelectionViewModel.class, moduleSelectionDialogFactory);
    dialogs.register(FileSaveViewModel.class, fileSaveDialogFactory);

    notifier = mock(Notifier.class);
  }

  @Test
  public void testExportModuleAction() throws IOException {
    ExportModuleAction action = new ExportModuleAction(
        moduleSource, projectPathResolver,
        zipper, dialogs, notifier
    );
    action.actionPerformed(event);

    ArgumentCaptor<ModuleSelectionViewModel> moduleSelectionDialogArg
        = ArgumentCaptor.forClass(ModuleSelectionViewModel.class);
    verify(moduleSelectionDialog).showAndGet(moduleSelectionDialogArg.capture());
    assertEquals(2, moduleSelectionDialogArg.getValue().getModules().length);

    ArgumentCaptor<FileSaveViewModel> fileSaveDialogArg
        = ArgumentCaptor.forClass(FileSaveViewModel.class);
    verify(fileSaveDialog).showAndGet(fileSaveDialogArg.capture());
    assertEquals(zipPath, fileSaveDialogArg.getValue().getPath());

    verify(moduleSource).getModules(same(project));
    verify(projectPathResolver).getProjectPath(same(project));
    verify(zipper).zipDirectory(eq(zipPath), eq(modulePath));
    verifyNoInteractions(notifier);
  }

  @Test
  public void testCancelModuleSelectionDialog() {
    cancelModuleSelection.set(true);
    ExportModuleAction action = new ExportModuleAction(
        moduleSource, projectPathResolver,
        zipper, dialogs, notifier
    );
    action.actionPerformed(event);

    verify(moduleSelectionDialog).showAndGet(any(ModuleSelectionViewModel.class));
    verifyNoInteractions(fileSaveDialog);
    verifyNoInteractions(projectPathResolver);
    verifyNoInteractions(zipper);
    verifyNoInteractions(notifier);
  }

  @Test
  public void testCancelFileSaveDialog() {
    cancelFileSave.set(true);
    ExportModuleAction action = new ExportModuleAction(
        moduleSource, projectPathResolver,
        zipper, dialogs, notifier
    );
    action.actionPerformed(event);

    verify(moduleSelectionDialog).showAndGet(any(ModuleSelectionViewModel.class));
    verify(fileSaveDialog).showAndGet(any(FileSaveViewModel.class));
    verifyNoInteractions(zipper);
    verifyNoInteractions(notifier);
  }

  @Test
  public void testNotifiesOfIoError() {
    IOException exception = new IOException("test exception");
    ExportModuleAction action = new ExportModuleAction(
        moduleSource,
        projectPathResolver,
        (outPath, inPath) -> {
          throw exception;
        },
        dialogs,
        notifier
    );
    action.actionPerformed(event);

    verify(moduleSelectionDialog).showAndGet(any(ModuleSelectionViewModel.class));
    verify(fileSaveDialog).showAndGet(any(FileSaveViewModel.class));
    verify(moduleSource).getModules(same(project));
    verify(projectPathResolver).getProjectPath(same(project));

    ArgumentCaptor<IoErrorNotification> notificationArg
        = ArgumentCaptor.forClass(IoErrorNotification.class);
    verify(notifier).notify(notificationArg.capture(), same(project));
    assertSame(exception, notificationArg.getValue().getException());
  }

}
