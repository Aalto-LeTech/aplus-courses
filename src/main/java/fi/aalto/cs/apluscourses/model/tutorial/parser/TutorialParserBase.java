package fi.aalto.cs.apluscourses.model.tutorial.parser;

import fi.aalto.cs.apluscourses.model.tutorial.Highlight;
import fi.aalto.cs.apluscourses.model.tutorial.Hint;
import fi.aalto.cs.apluscourses.model.tutorial.Observer;
import fi.aalto.cs.apluscourses.model.tutorial.Transition;
import fi.aalto.cs.apluscourses.model.tutorial.Tutorial;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialClientObject;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialFactory;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialObject;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialScene;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialState;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitchImpl;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import fi.aalto.cs.apluscourses.utils.dom.DomParser;
import fi.aalto.cs.apluscourses.utils.dom.Node;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TutorialParserBase<S extends TutorialScope>
    extends DomParser<TutorialObject, S> implements TutorialParser {

  public final @NotNull TutorialFactory factory;

  @Override
  protected void init() {
    registerObjectParser("tutorial", this::parseTutorial);
    registerObjectParser("state", this::parseState);
    registerObjectParser("scene", this::parseScene);
    registerObjectParser("hint", this::parseHint);
    registerObjectParser("transition", this::parseTransition);
    registerObjectParser("highlight", this::parseHighlight);
    registerObjectParser("code", this::parseCodeObserver);
    registerObjectParser("file", this::parseFileObserver);
    registerObjectParser("build", this::parseBuildObserver);
    registerObjectParser("breakpoint", this::parseBreakpointObserver);
    registerObjectParser("debug", this::parseDebugObserver);
    registerObjectParser("debugger", this::parseDebuggerObserver);

    registerScopeParser("component", this::parseComponentScope);
    registerScopeParser("state", this::parseStateScope);
  }

  protected TutorialParserBase(@NotNull TutorialFactory factory) {
    this.factory = factory;
  }

  @Override
  public @Nullable Tutorial parse(@NotNull Node rootNode, @NotNull TutorialComponent rootComponent) {
    var result = CollectionUtil.findSingle(
        CollectionUtil.ofType(parse(rootNode, createInitialScope(rootComponent)), Tutorial.class));
    return result.orElse(null);
  }

  private @NotNull Tutorial parseTutorial(@NotNull Node node,
                                          @NotNull List<@NotNull TutorialObject> children,
                                          @NotNull S scope) {
    var states = CollectionUtil.ofType(children, TutorialState.class);
    var objects = CollectionUtil.ofType(children, TutorialClientObject.class);
    var initialStateKey = node.getProp("start");
    var tutorial = factory.createTutorial(states, objects, initialStateKey);
    scope.getStateSwitch().connect(tutorial);
    return tutorial;
  }

  private @NotNull TutorialState parseState(@NotNull Node node,
                                            @NotNull List<@NotNull TutorialObject> children,
                                            @NotNull S scope) {
    var key = node.getProp("key");
    var scenes = CollectionUtil.ofType(children, TutorialScene.class);
    var objects = CollectionUtil.ofType(children, TutorialClientObject.class);
    var state = factory.createState(key, scenes, objects);
    scope.getSceneSwitch().connect(state);
    return state;
  }

  private TutorialObject parseScene(@NotNull Node node,
                                    @NotNull List<TutorialObject> children,
                                    @NotNull S scope) {
    var objects = CollectionUtil.ofType(children, TutorialClientObject.class);
    return factory.createScene(objects);
  }

  private @NotNull Hint parseHint(@NotNull Node node,
                                  @NotNull List<@NotNull TutorialObject> children,
                                  @NotNull S scope) {
    var content = node.getContent();
    var title = node.optProp("title");
    boolean isNavigable = node.parseProp("navigable", Boolean::parseBoolean, false);
    var component = scope.getComponent();
    return factory.createHint(content, title, isNavigable ? scope.getSceneSwitch() : null, component);
  }

  private @NotNull Transition parseTransition(@NotNull Node node,
                                              @NotNull List<@NotNull TutorialObject> children,
                                              @NotNull S scope) {
    var goTo = node.getProp("goto");
    var component = scope.getComponent();
    var stateSwitch = scope.getStateSwitch();
    var observers = CollectionUtil.ofType(children, Observer.class);
    return factory.createTransition(goTo, stateSwitch, observers, component);
  }

  private @NotNull Highlight parseHighlight(@NotNull Node node,
                                            @NotNull List<@NotNull TutorialObject> children,
                                            @NotNull S scope) {
    var degree = Highlight.Degree.valueOf(node.getProp("degree"));
    var component = scope.getComponent();
    return factory.createHighlight(degree, component);
  }

  private @NotNull Observer parseCodeObserver(@NotNull Node node,
                                              @NotNull List<@NotNull TutorialObject> children,
                                              @NotNull S scope) {
    var lang = node.getProp("lang");
    var code = node.getContent();
    var component = scope.getComponent();
    return factory.createCodeObserver(lang, code, component);
  }

  private @NotNull Observer parseFileObserver(@NotNull Node node,
                                              @NotNull List<@NotNull TutorialObject> children,
                                              @NotNull S scope) {
    var action = node.getProp("action");
    var path = node.getProp("path");
    var component = scope.getComponent();
    return factory.createFileObserver(action, path, component);
  }

  private @NotNull Observer parseBuildObserver(@NotNull Node node,
                                               @NotNull List<@NotNull TutorialObject> children,
                                               @NotNull S scope) {
    var action = node.getProp("action");
    var component = scope.getComponent();
    return factory.createBuildObserver(action, component);
  }

  private @NotNull Observer parseBreakpointObserver(@NotNull Node node,
                                                          @NotNull List<@NotNull TutorialObject> children,
                                                          @NotNull S scope) {
    var component = scope.getComponent();
    return factory.createBreakpointObserver(component);
  }

  private @NotNull Observer parseDebugObserver(@NotNull Node node,
                                               @NotNull List<@NotNull TutorialObject> tutorialObjects,
                                               @NotNull S scope) {
    var action = node.getProp("action");
    var component = scope.getComponent();
    return factory.createDebugObserver(action, component);
  }

  private @NotNull Observer parseDebuggerObserver(@NotNull Node node,
                                                  @NotNull List<@NotNull TutorialObject> tutorialObjects,
                                                  @NotNull S scope) {
    var action = node.getProp("action");
    var component = scope.getComponent();
    return factory.createDebuggerObserver(action, component);
  }

  private @NotNull S parseComponentScope(@NotNull Node node, @NotNull S scope) {
    var component = factory.createComponent(node.getProp("type"), node);
    return createModifiedScope(component, null, null, scope);
  }

  private @NotNull S parseStateScope(@NotNull Node node, @NotNull S scope) {
    return createModifiedScope(null, null, new SceneSwitchImpl(), scope);
  }

  protected abstract @NotNull S createModifiedScope(@Nullable TutorialComponent component,
                                                    @Nullable StateSwitch stateSwitch,
                                                    @Nullable SceneSwitch sceneSwitch,
                                                    @NotNull S scope);

  protected abstract @NotNull S createInitialScope(@NotNull TutorialComponent component);
}
