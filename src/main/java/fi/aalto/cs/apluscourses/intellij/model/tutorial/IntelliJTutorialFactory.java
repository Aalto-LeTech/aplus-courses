package fi.aalto.cs.apluscourses.intellij.model.tutorial;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XSourcePosition;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJBuildButton;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJEditor;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJEditorBlock;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJTutorialComponent;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.component.IntelliJWindow;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.observer.IntelliJBreakpointObserver;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.observer.IntelliJBuildObserver;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.observer.IntelliJCodeObserver;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.observer.IntelliJDebugObserver;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.observer.IntelliJDebuggerObserver;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.observer.IntelliJFileObserver;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import fi.aalto.cs.apluscourses.model.tutorial.Highlight;
import fi.aalto.cs.apluscourses.model.tutorial.Hint;
import fi.aalto.cs.apluscourses.model.tutorial.LineRange;
import fi.aalto.cs.apluscourses.model.tutorial.Observer;
import fi.aalto.cs.apluscourses.model.tutorial.Tutorial;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialClientObject;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponentFactory;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialFactoryBase;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialState;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.ui.tutorials.OverlayPane;
import java.awt.Component;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJTutorialFactory extends TutorialFactoryBase<IntelliJTutorialComponent<?>>
    implements TutorialComponentFactory {
  private final @NotNull OverlayPane overlayPane;
  private final @Nullable Project project;

  public IntelliJTutorialFactory(@NotNull OverlayPane overlayPane, @Nullable Project project) {
    this.overlayPane = overlayPane;
    this.project = project;
  }

  @Override
  protected @NotNull TutorialComponentFactory getComponentFactory() {
    return this;
  }

  @Override
  public @NotNull Tutorial createTutorial(@NotNull Collection<@NotNull TutorialState> states,
                                          @NotNull Collection<@NotNull TutorialClientObject> objects,
                                          @NotNull String initialStateKey) {
    return new IntelliJTutorial(states, objects, initialStateKey, overlayPane);
  }

  @Override
  protected @NotNull Hint createHintOverride(@NotNull String content,
                                             @Nullable String title,
                                             @Nullable SceneSwitch sceneSwitch,
                                             @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJHint(content, title, sceneSwitch, component, overlayPane);
  }

  @Override
  protected @NotNull Highlight createHighlightOverride(@NotNull Highlight.Degree degree,
                                                       @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJHighlight(degree, component, overlayPane);
  }

  @Override
  protected @NotNull IntelliJTutorialComponent<?> castTutorialComponent(@NotNull TutorialComponent component) {
    return (IntelliJTutorialComponent<?>) component;
  }

  @Override
  public @NotNull TutorialComponent createEditor(@Nullable Path path) {
    return new IntelliJEditor(path, project);
  }

  @Override
  public @NotNull TutorialComponent createWindow() {
    return new IntelliJWindow(project);
  }

  @Override
  public @NotNull TutorialComponent createProjectTree() {
    return new IntelliJTutorialComponent<>(project) {
      @Override
      protected @Nullable Component getAwtComponent() {
        return Optional.ofNullable(getProject())
            .map(ProjectView::getInstance)
            .map(ProjectView::getCurrentProjectViewPane)
            .map(AbstractProjectViewPane::getTree)
            .orElse(null);
      }
    };
  }

  @Override
  public @NotNull TutorialComponent createEditorBlock(@Nullable Path path, @NotNull LineRange lineRange) {
    return new IntelliJEditorBlock(path, lineRange, project);
  }

  @Override
  public @NotNull TutorialComponent createBuildButton() {
    return new IntelliJBuildButton(project);
  }

  @Override
  public @NotNull Observer createCodeObserverOverride(@NotNull String lang,
                                                      @NotNull String code,
                                                      @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJCodeObserver(lang, code, component);
  }

  @Override
  protected @NotNull Observer createFileObserverOverride(@NotNull String action,
                                                         @NotNull String pathSuffix,
                                                         @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJFileObserver(action, pathSuffix, component);
  }

  @Override
  protected @NotNull Observer createBuildObserverOverride(@NotNull String action,
                                                          @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJBuildObserver(action, component);
  }

  @Override
  protected @NotNull Observer createBreakpointObserverOverride(@NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJBreakpointObserver(component);
  }

  @Override
  protected @NotNull Observer createDebugObserverOverride(@NotNull String action,
                                                          @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJDebugObserver(action, component);
  }

  @Override
  protected @NotNull Observer createDebuggerObserverOverride(@NotNull String action,
                                                             @NotNull IntelliJTutorialComponent<?> component) {
    return new IntelliJDebuggerObserver(action, component);
  }

}
