package fi.aalto.cs.apluscourses.presentation.commands;

import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import org.jetbrains.annotations.NotNull;

public interface MainViewModelContext extends Command.Context {
  @NotNull
  MainViewModel getMainViewModel();
}
