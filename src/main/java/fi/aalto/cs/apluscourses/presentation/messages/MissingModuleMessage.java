package fi.aalto.cs.apluscourses.presentation.messages;

import org.jetbrains.annotations.NotNull;

public class MissingModuleMessage implements Message {
  @NotNull
  private final String moduleName;

  public MissingModuleMessage(@NotNull String moduleName) {
    this.moduleName = moduleName;
  }

  @NotNull
  @Override
  public String getContent() {
    return "A+ Courses plugin couldn't find the module " + moduleName + ".";
  }

  @NotNull
  @Override
  public String getTitle() {
    return "Could not find module";
  }

  @NotNull
  @Override
  public Level getLevel() {
    return Level.ERR;
  }

  @NotNull
  public String getModuleName() {
    return moduleName;
  }
}
