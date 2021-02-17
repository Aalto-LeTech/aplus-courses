package fi.aalto.cs.apluscourses.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class Library extends Component {

  public Library(@NotNull String name) {
    super(name);
  }

  @NotNull
  @Override
  protected List<String> computeDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isUpdatable() {
    return false;
  }

  @Override
  public boolean hasLocalChanges() {
    return false;
  }
}
