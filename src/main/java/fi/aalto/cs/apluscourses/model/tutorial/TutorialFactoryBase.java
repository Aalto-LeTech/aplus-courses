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

  protected abstract @NotNull TutorialObserverFactory<C> getObserverFactory();

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
  public @NotNull C createComponent(@NotNull String type, @NotNull Props props, @Nullable TutorialComponent parent) {
    return castTutorialComponent(createComponentInternal(type, props, parent));
  }

  private @NotNull TutorialComponent createComponentInternal(@NotNull String type,
                                                             @NotNull Props props,
                                                             @Nullable TutorialComponent parent) {
    var factory = getComponentFactory();
    CodeRange codeRange;
    switch (type) {
      case "editor":
        return factory.createEditor(props.parseProp("path", Path::of, null), parent);
      case "editor.block":
        codeRange = parseLineRange(props.getProp("lines"), parent);
        return factory.createEditorBlock(codeRange, parent);
      case "window":
        return factory.createWindow(parent);
      case "project-tree":
        return factory.createProjectTree(parent);
      case "build-button":
        return factory.createBuildButton(parent);
      case "editor.run":
        codeRange = parseLineRange(props.getProp("lines"), parent);
        return factory.createRunLineButton(codeRange, parent);
      case "run-window":
        return factory.createRunWindow(parent);
      default:
        throw new IllegalArgumentException("Unknown component type: " + type);
    }
  }

  protected abstract @NotNull CodeRange parseLineRange(@NotNull String s, @Nullable TutorialComponent parent);

  @Override
  public @NotNull Observer createObserver(@NotNull String type,
                                          @NotNull String content,
                                          @NotNull Props props,
                                          @NotNull TutorialComponent component) {
    return createObserverInternal(type, content, props, castTutorialComponent(component));
  }

  private @NotNull Observer createObserverInternal(@NotNull String type,
                                                   @NotNull String content,
                                                   @NotNull Props props,
                                                   @NotNull C component) {
    var factory = getObserverFactory();
    switch (type) {
      case "code":
        return factory.createCodeObserver(props.getProp("lang"), content, component);
      case "file":
        return factory.createFileObserver(props.getProp("action"),
                                          props.getProp("path"),
                                          component);
      case "build":
        return factory.createBuildObserver(props.getProp("action"), component);
      case "breakpoint":
        return factory.createBreakpointObserver(component);
      case "debug":
        return factory.createDebugObserver(props.getProp("action"), component);
      case "debugger":
        return factory.createDebuggerObserver(props.getProp("action"), component);
      case "run":
        return factory.createRunObserver(props.getProp("action"), component);
      default:
        throw new IllegalArgumentException("Unknown observer type: " + type);
    }
  }

  @Override
  public @NotNull Hint createHint(@NotNull String content,
                                  @Nullable String title,
                                  boolean keepVisible,
                                  @NotNull List<@NotNull Transition> transitions,
                                  @Nullable SceneSwitch sceneSwitch,
                                  @NotNull TutorialComponent component) {
    return createHintOverride(content, title, keepVisible, transitions, sceneSwitch, castTutorialComponent(component));
  }

  @Override
  public @NotNull Transition createTransition(@Nullable String label,
                                              @NotNull String goTo,
                                              @NotNull StateSwitch stateSwitch,
                                              @NotNull Collection<@NotNull Observer> observers,
                                              @NotNull TutorialComponent component) {
    return new Transition(label, goTo, stateSwitch, observers, component);
  }

  @Override
  public @NotNull Highlight createHighlight(Highlight.@NotNull Degree degree, @NotNull TutorialComponent component) {
    return createHighlightOverride(degree, castTutorialComponent(component));
  }

  protected abstract @NotNull Hint createHintOverride(@NotNull String content,
                                                      @Nullable String title,
                                                      boolean keepVisible,
                                                      @NotNull List<@NotNull Transition> transitions,
                                                      @Nullable SceneSwitch sceneSwitch,
                                                      @NotNull C component);

  protected abstract @NotNull Highlight createHighlightOverride(Highlight.@NotNull Degree degree,
                                                                @NotNull C castTutorialComponent);

  protected abstract @NotNull C castTutorialComponent(@NotNull TutorialComponent component);
}
