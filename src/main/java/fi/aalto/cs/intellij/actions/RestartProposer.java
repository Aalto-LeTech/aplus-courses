package fi.aalto.cs.intellij.actions;

/**
 * An abstract interface for an object that proposes the IDE restart. The most useful realizations
 * of this interface is {@code InstallPluginsNotificationAction::proposeRestart} and {@code
 * EnablePluginsNotificationAction::proposeRestart}.
 */
@FunctionalInterface
public interface RestartProposer {

  /**
   * Calls for an IJ framework to trigger a restart of the IDE.
   */
  void proposeRestart();
}