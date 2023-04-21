package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TutorialFactoryBase<C extends TutorialComponent> implements TutorialFactory {
  protected abstract @NotNull TutorialComponentFactory getComponentFactory();

  @Override
  public @NotNull Tutorial createTutorial(@NotNull Collection<@NotNull TutorialState> states,
                                          @NotNull Collection<@NotNull TutorialClientObject> objects,
                                          @NotNull String initialStateKey) {
    return new Tutorial(states, objects, initialStateKey);
  }

  @Override
  public @NotNull TutorialState createState(@NotNull String key,
                                            @NotNull List<@NotNull TutorialScene> scenes,
                                            @NotNull Collection<@NotNull TutorialClientObject> objects) {
    return new TutorialStateImpl(key, scenes, objects);
  }

  @Override
  public @NotNull TutorialScene createScene(@NotNull Collection<TutorialClientObject> objects) {
    return new TutorialScene(objects);
  }

  @Override
  public @NotNull C createComponent(@NotNull String type, @NotNull Props props) {
    return castTutorialComponent(createComponentInternal(type, props));
  }

  private @NotNull TutorialComponent createComponentInternal(@NotNull String type, @NotNull Props props) {
    var tutorialComponentFactory = getComponentFactory();
    switch (type) {
      case "editor":
        return tutorialComponentFactory.createEditor(props.parseProp("path", Path::of, null));
      case "editor-block":
        return tutorialComponentFactory.createEditorBlock(props.parseProp("path", Path::of, null),
                                                          props.parseProp("lines", LineRange::parse));
      case "window":
        return tutorialComponentFactory.createWindow();
      case "project-tree":
        return tutorialComponentFactory.createProjectTree();
      case "build-button":
        return tutorialComponentFactory.createBuildButton();
      default:
        throw new IllegalArgumentException("Unknown component type.");
    }
  }

  @Override
  public @NotNull Hint createHint(@NotNull String content,
                                  @Nullable String title,
                                  @Nullable SceneSwitch sceneSwitch,
                                  @NotNull TutorialComponent component) {
    return createHintOverride(content, title, sceneSwitch, castTutorialComponent(component));
  }

  @Override
  public @NotNull Transition createTransition(@NotNull String goTo,
                                              @NotNull StateSwitch stateSwitch,
                                              @NotNull Collection<@NotNull Observer> observers,
                                              @NotNull TutorialComponent component) {
    return new Transition(goTo, stateSwitch, observers, component);
  }

  @Override
  public @NotNull Highlight createHighlight(Highlight.@NotNull Degree degree, @NotNull TutorialComponent component) {
    return createHighlightOverride(degree, castTutorialComponent(component));
  }

  @Override
  public @NotNull Observer createCodeObserver(@NotNull String lang,
                                              @NotNull String code,
                                              @NotNull TutorialComponent component) {
    return createCodeObserverOverride(lang, code, castTutorialComponent(component));
  }

  @Override
  public @NotNull Observer createFileObserver(@NotNull String action,
                                              @NotNull String pathSuffix,
                                              @NotNull TutorialComponent component) {
    return createFileObserverOverride(action, pathSuffix, castTutorialComponent(component));
  }

  @Override
  public @NotNull Observer createBuildObserver(@NotNull String action,
                                               @NotNull TutorialComponent component) {
    return createBuildObserverOverride(action, castTutorialComponent(component));
  }

  @Override
  public @NotNull Observer createBreakpointObserver(@NotNull TutorialComponent component) {
    return createBreakpointObserverOverride(castTutorialComponent(component));
  }

  @Override
  public @NotNull Observer createDebugObserver(@NotNull String action,
                                               @NotNull TutorialComponent component) {
    return createDebugObserverOverride(action, castTutorialComponent(component));
  }

  @Override
  public @NotNull Observer createDebuggerObserver(@NotNull String action,
                                                  @NotNull TutorialComponent component) {
    return createDebuggerObserverOverride(action, castTutorialComponent(component));
  }

  protected abstract @NotNull Hint createHintOverride(@NotNull String content,
                                                      @Nullable String title,
                                                      @Nullable SceneSwitch sceneSwitch,
                                                      @NotNull C component);

  protected abstract @NotNull Highlight createHighlightOverride(Highlight.@NotNull Degree degree,
                                                                @NotNull C castTutorialComponent);

  protected abstract @NotNull C castTutorialComponent(@NotNull TutorialComponent component);

  protected abstract @NotNull Observer createCodeObserverOverride(@NotNull String lang,
                                                                  @NotNull String code,
                                                                  @NotNull C component);

  protected abstract @NotNull Observer createFileObserverOverride(@NotNull String action,
                                                                  @NotNull String pathSuffix,
                                                                  @NotNull C castTutorialComponent);

  protected abstract @NotNull Observer createBuildObserverOverride(@NotNull String action,
                                                                   @NotNull C component);

  protected abstract @NotNull Observer createBreakpointObserverOverride(@NotNull C component);

  protected abstract @NotNull Observer createDebugObserverOverride(@NotNull String action,
                                                                   @NotNull C component);

  protected abstract @NotNull Observer createDebuggerObserverOverride(@NotNull String action,
                                                                      @NotNull C component);

}
