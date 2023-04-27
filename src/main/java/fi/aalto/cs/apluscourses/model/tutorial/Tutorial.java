package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.parser.DefaultTutorialParser;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.dom.XmlDomNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

public class Tutorial implements TutorialObject, StateSwitch {
  public static final String TUTORIAL_SUBMIT_FILE_NAME = "_ideact_result";

  public final @NotNull Event completed = new Event();
  private @NotNull TutorialState currentState;
  private final @NotNull Map<@NotNull String, @NotNull TutorialState> states;
  private final @NotNull List<@NotNull TutorialClientObject> objects;
  private final @NotNull String initialStateKey;

  public Tutorial(@NotNull Collection<@NotNull TutorialState> states,
                  @NotNull Collection<@NotNull TutorialClientObject> objects,
                  @NotNull String initialStateKey) {
    this.states = states.stream().collect(Collectors.toMap(TutorialState::getKey, Function.identity()));
    this.objects = List.copyOf(objects);
    this.initialStateKey = initialStateKey;
    this.currentState = EmptyTutorialState.INSTANCE;
  }

  @NotNull
  public String getSubmissionPayload() {
    return "success";
  }

  @Override
  public void goTo(@NotNull String key) {
    var state = states.get(key);
    if (state == null) {
      complete();
    } else {
      goTo(state);
    }
  }

  private void goTo(@NotNull TutorialState state) {
    currentState.deactivate();
    currentState = state;
    currentState.activate();
  }

  public void complete() {
    deactivate();
    this.completed.trigger();
  }

  @Override
  public void activate() {
    goTo(initialStateKey);
    this.objects.forEach(TutorialObject::activate);
  }

  @Override
  public void deactivate() {
    goTo(EmptyTutorialState.INSTANCE);
    this.objects.forEach(TutorialObject::deactivate);
  }

  public static Tutorial fromStream(@NotNull InputStream stream, @NotNull TutorialFactory tutorialFactory) throws IOException {
    try {
      return new DefaultTutorialParser(tutorialFactory)
          .parse(XmlDomNode.read(stream),
                 tutorialFactory.createComponent("window", Props.EMPTY, null));
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }
}
