package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.StateMonitor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class Component {
  public static final int UNRESOLVED = StateMonitor.INITIAL;
  public static final int NOT_INSTALLED = UNRESOLVED + 1;
  public static final int FETCHING = NOT_INSTALLED + 1;
  public static final int FETCHED = FETCHING + 1;
  public static final int LOADING = FETCHED + 1;
  public static final int LOADED = LOADING + 1;
  public static final int ERROR = StateMonitor.ERROR;

  public static final int DEP_INITIAL = StateMonitor.INITIAL;
  public static final int DEP_WAITING = DEP_INITIAL + 1;
  public static final int DEP_VALIDATING = DEP_WAITING + 1;
  public static final int DEP_LOADED = DEP_VALIDATING + 1;
  public static final int DEP_ERROR = StateMonitor.ERROR;

  public final Event stateChanged = new Event();
  public final Event onError = new Event();

  public final StateMonitor stateMonitor = new StateMonitor(UNRESOLVED, this::onStateChanged);
  public final StateMonitor dependencyStateMonitor =
      new StateMonitor(DEP_INITIAL, this::onStateChanged);

  @NotNull
  protected final String name;

  private List<String> dependencies;

  public Component(@NotNull String name) {
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public abstract Path getPath();

  public abstract void fetch() throws IOException;

  public abstract void load() throws ComponentLoadException;

  protected void onStateChanged(int newState) {
    stateChanged.trigger();
    if (StateMonitor.isError(newState)) {
      onError.trigger();
    }
  }

  /**
   * Tells whether the component is in an error state.
   * @return True if error, otherwise false.
   */
  public boolean hasError() {
    int state = stateMonitor.get();
    int dependencyState = dependencyStateMonitor.get();
    return StateMonitor.isError(state) || state == LOADED && StateMonitor.isError(dependencyState);
  }

  /**
   * Returns the names of the dependencies.  This method should not be called unless the component
   * is in LOADED state.
   *
   * @return Names of the dependencies, as a {@link List}.
   */
  @NotNull
  public List<String> getDependencies() {
    if (dependencies == null) {
      dependencies = computeDependencies();
    }
    return dependencies;
  }

  protected abstract int resolveStateInternal();

  /**
   * If the state is UNRESOLVED, sets it to a state resolved by subclasses.
   */
  public void resolveState() {
    if (stateMonitor.get() == UNRESOLVED) {
      stateMonitor.setConditionally(UNRESOLVED, resolveStateInternal());
    }
  }

  protected abstract List<String> computeDependencies();

  /**
   * Checks whether this component conforms the dependency integrity constraint, that is, its
   * dependencies are in LOADED state.
   * @param componentSource A component source which should have the dependencies of this component.
   * @return True if the integrity is conformed, otherwise false.
   */
  public boolean checkDependencyIntegrity(ComponentSource componentSource) {
    return dependencyStateMonitor.get() < DEP_VALIDATING || getDependencies().stream()
        .map(componentSource::getComponentIfExists)
        .allMatch(component -> component != null && component.stateMonitor.get() == LOADED);
  }

  public void setUnresolved() {
    stateMonitor.set(UNRESOLVED);
  }
}
