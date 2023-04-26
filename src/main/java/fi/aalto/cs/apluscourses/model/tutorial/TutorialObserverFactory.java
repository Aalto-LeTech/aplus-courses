package fi.aalto.cs.apluscourses.model.tutorial;

import org.jetbrains.annotations.NotNull;

public interface TutorialObserverFactory<C extends TutorialComponent> {
  @NotNull Observer createCodeObserver(@NotNull String lang,
                                       @NotNull String code,
                                       @NotNull C component);

  @NotNull Observer createFileObserver(@NotNull String action,
                                       @NotNull String pathSuffix,
                                       @NotNull C component);

  @NotNull Observer createBuildObserver(@NotNull String action,
                                        @NotNull C component);

  @NotNull Observer createBreakpointObserver(@NotNull C component);

  @NotNull Observer createDebugObserver(@NotNull String action, @NotNull C component);

  @NotNull Observer createDebuggerObserver(@NotNull String action, @NotNull C component);

  @NotNull Observer createRunObserver(@NotNull String action, @NotNull C component);
}
