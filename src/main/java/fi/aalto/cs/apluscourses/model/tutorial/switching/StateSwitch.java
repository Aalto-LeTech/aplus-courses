package fi.aalto.cs.apluscourses.model.tutorial.switching;

import org.jetbrains.annotations.NotNull;

public interface StateSwitch extends Switch<StateSwitch> {
  void goTo(@NotNull String key);

}
