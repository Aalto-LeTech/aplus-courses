package fi.aalto.cs.apluscourses.model.tutorial;

import fi.aalto.cs.apluscourses.model.tutorial.switching.StateSwitch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Transition extends TutorialClientObjectBase {
  private final @Nullable String label;
  private final @NotNull String goTo;
  private final @NotNull StateSwitch stateSwitch;
  private final @NotNull Collection<@NotNull Observer> observers;

  public Transition(@Nullable String label,
                    @NotNull String goTo,
                    @NotNull StateSwitch stateSwitch,
                    @NotNull Collection<@NotNull Observer> observers,
                    @NotNull TutorialComponent component) {
    super(component);
    this.label = label;
    this.goTo = goTo;
    this.stateSwitch = stateSwitch;
    this.observers = observers;
    for (var observer : observers) {
      observer.setHandler(this::go);
    }
  }

  public @Nullable String getLabel() {
    return label;
  }

  public void go() {
    stateSwitch.goTo(goTo);
  }

  @Override
  public void activate() {
    this.observers.forEach(Observer::activate);
  }

  @Override
  public void deactivate() {
    this.observers.forEach(Observer::deactivate);
  }
}
