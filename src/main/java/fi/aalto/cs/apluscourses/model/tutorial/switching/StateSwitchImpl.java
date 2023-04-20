package fi.aalto.cs.apluscourses.model.tutorial.switching;

import org.jetbrains.annotations.NotNull;

public class StateSwitchImpl extends SwitchBase<StateSwitch> implements StateSwitch {

  @Override
  public void goTo(@NotNull String key) {
    delegate(StateSwitch::goTo, key);
  }
}
