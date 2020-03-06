package fi.aalto.cs.apluscourses.model;

import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.StateMonitor;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public abstract class Installable {
  public static final int ERROR = StateMonitor.ERROR;
  public static final int NOT_INSTALLED = StateMonitor.INITIAL;
  public static final int FETCHING = NOT_INSTALLED + 1;
  public static final int FETCHED = FETCHING + 1;
  public static final int LOADING = FETCHED + 1;
  public static final int LOADED = LOADING + 1;
  public static final int WAITING_FOR_DEPS = LOADED + 1;
  public static final int INSTALLED = WAITING_FOR_DEPS + 1;
  public final Event stateChanged = new Event();
  public final StateMonitor stateMonitor = new StateMonitor(this::onStateChanged);
  @NotNull
  protected final String name;

  public Installable(@NotNull String name) {
    this.name = name;
  }


  @NotNull
  public String getName() {
    return name;
  }

  public abstract void fetch() throws IOException;

  public abstract void load() throws ModuleLoadException;

  protected void onStateChanged() {
    stateChanged.trigger();
  }

  public boolean hasError() {
    return stateMonitor.get() <= ERROR;
  }

  /**
   * Returns the names of the dependencies.  This method should not be called unless the installable
   * is in FETCHED state or further.
   *
   * @return Names of the dependencies, as a {@link List}.
   * @throws ModuleLoadException If dependencies could not be read.
   */
  @NotNull
  public abstract List<String> getDependencies() throws ModuleLoadException;
}
