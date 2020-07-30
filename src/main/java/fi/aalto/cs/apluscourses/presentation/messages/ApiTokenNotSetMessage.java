package fi.aalto.cs.apluscourses.presentation.messages;

import org.jetbrains.annotations.NotNull;

public class ApiTokenNotSetMessage implements Message {
  @NotNull
  @Override
  public String getContent() {
    return "Token could not be persistently stored. You will be requested to paste the token again "
        + "next time you'll open the project. To allow token to be securely stored in your "
        + "machine, check your keyring settings.";
  }

  @NotNull
  @Override
  public String getTitle() {
    return "API token not stored";
  }

  @NotNull
  @Override
  public Level getLevel() {
    return Level.INFO;
  }
}
