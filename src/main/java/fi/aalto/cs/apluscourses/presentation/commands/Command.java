package fi.aalto.cs.apluscourses.presentation.commands;

import fi.aalto.cs.apluscourses.presentation.dialogs.Dialogs;
import fi.aalto.cs.apluscourses.presentation.messages.Messenger;
import org.jetbrains.annotations.NotNull;

public interface Command<T extends Command.Context> {
  boolean canExecute(@NotNull T context);

  void execute(@NotNull T context);

  interface Context {

    Dialogs getDialogs();

    Messenger getMessenger();
  }
}
