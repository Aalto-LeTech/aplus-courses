package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.switching.SceneSwitch;
import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TutorialFactory {
  @NotNull Tutorial createTutorial(@NotNull Collection<@NotNull TutorialState> states,
                                   @NotNull Collection<@NotNull TutorialClientObject> objects,
                                   @NotNull String initialStateKey);
  
  @NotNull TutorialState createState(@NotNull String key,
                                     @NotNull List<@NotNull TutorialScene> scenes,
                                     @NotNull Collection<@NotNull TutorialClientObject> objects);

  @NotNull TutorialScene createScene(@NotNull Collection<TutorialClientObject> objects);

  @NotNull TutorialComponent createComponent(@NotNull String type,
                                             @NotNull Props props,
                                             @Nullable TutorialComponent parent);

  @NotNull Hint createHint(@NotNull String content,
                           @Nullable String title,
                           boolean keepVisible,
                           @NotNull List<@NotNull Transition> transitions,
                           @Nullable SceneSwitch sceneSwitch,
                           @NotNull TutorialComponent component);

  @NotNull Transition createTransition(@Nullable String label,
                                       @NotNull String goTo,
                                       @NotNull StateSwitch stateSwitch,
                                       @NotNull Collection<@NotNull Observer> observers,
                                       @NotNull TutorialComponent component);

  @NotNull Highlight createHighlight(@NotNull Highlight.Degree degree, @NotNull TutorialComponent component);

  @NotNull Observer createObserver(@NotNull String type,
                                   @NotNull String content,
                                   @NotNull Props props,
                                   @NotNull TutorialComponent component);
}
