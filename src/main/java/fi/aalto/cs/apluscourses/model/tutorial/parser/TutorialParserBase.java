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
    registerObjectParser("observer", this::parseObserver);

    registerScopeParser("component", this::parseComponentScope);
    registerScopeParser("state", this::parseStateScope);
  }

  protected TutorialParserBase(@NotNull TutorialFactory factory) {
    this.factory = factory;
  }

  @Override
  public @Nullable Tutorial parse(@NotNull Node rootNode, @NotNull TutorialComponent rootComponent) {
    var result = CollectionUtil.findSingle(
        CollectionUtil.ofType(Tutorial.class, parse(rootNode, createInitialScope(rootComponent))));
    return result.orElse(null);
  }

  private @NotNull Tutorial parseTutorial(@NotNull Node node,
                                          @NotNull List<@NotNull TutorialObject> children,
                                          @NotNull S scope) {
    var states = CollectionUtil.ofType(TutorialState.class, children);
    var objects = CollectionUtil.ofType(TutorialClientObject.class, children);
    var initialStateKey = node.getProp("start");
    var tutorial = factory.createTutorial(states, objects, initialStateKey);
    scope.getStateSwitch().connect(tutorial);
    return tutorial;
  }

  private @NotNull TutorialState parseState(@NotNull Node node,
                                            @NotNull List<@NotNull TutorialObject> children,
                                            @NotNull S scope) {
    var key = node.getProp("key");
    var scenes = CollectionUtil.ofType(TutorialScene.class, children);
    var objects = CollectionUtil.ofType(TutorialClientObject.class, children);
    var state = factory.createState(key, scenes, objects);
    scope.getSceneSwitch().connect(state);
    return state;
  }

  private TutorialObject parseScene(@NotNull Node node,
                                    @NotNull List<TutorialObject> children,
                                    @NotNull S scope) {
    var objects = CollectionUtil.ofType(TutorialClientObject.class, children);
    return factory.createScene(objects);
  }

  private @NotNull Hint parseHint(@NotNull Node node,
                                  @NotNull List<@NotNull TutorialObject> children,
                                  @NotNull S scope) {
    var content = node.getContent();
    var transitions = CollectionUtil.ofType(Transition.class, children);
    var title = node.optProp("title");
    boolean keepVisible = node.parseProp("keep-visible", Boolean::parseBoolean, false);
    boolean navigable = node.parseProp("navigable", Boolean::parseBoolean, false);
    var component = scope.getComponent();
    return factory.createHint(content,
        title,
        keepVisible,
        transitions,
        navigable ? scope.getSceneSwitch() : null,
        component);
  }

  private @NotNull Transition parseTransition(@NotNull Node node,
                                              @NotNull List<@NotNull TutorialObject> children,
                                              @NotNull S scope) {
    var label = node.optProp("label");
    var goTo = node.getProp("goto");
    var component = scope.getComponent();
    var stateSwitch = scope.getStateSwitch();
    var observers = CollectionUtil.ofType(Observer.class, children);
    return factory.createTransition(label, goTo, stateSwitch, observers, component);
  }

  private @NotNull Highlight parseHighlight(@NotNull Node node,
                                            @NotNull List<@NotNull TutorialObject> children,
                                            @NotNull S scope) {
    var degree = Highlight.Degree.valueOf(node.getProp("degree"));
    var component = scope.getComponent();
    return factory.createHighlight(degree, component);
  }

  private @NotNull Observer parseObserver(@NotNull Node node,
                                          @NotNull List<@NotNull TutorialObject> children,
                                          @NotNull S scope) {
    var type = node.getProp("type");
    var content = node.getContent();
    var component = scope.getComponent();
    return factory.createObserver(type, content, node, component);
  }

  private @NotNull S parseComponentScope(@NotNull Node node, @NotNull S scope) {
    var parent = scope.getComponent();
    var component = factory.createComponent(node.getProp("type"), node, parent);
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
