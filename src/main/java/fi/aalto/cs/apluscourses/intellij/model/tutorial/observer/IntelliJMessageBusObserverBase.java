package fi.aalto.cs.apluscourses.intellij.model.tutorial.observer;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import fi.aalto.cs.apluscourses.intellij.model.tutorial.IntelliJTutorialClientObject;
import fi.aalto.cs.apluscourses.model.tutorial.Observer;
import fi.aalto.cs.apluscourses.model.tutorial.TutorialComponent;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public abstract class IntelliJMessageBusObserverBase<T> extends Observer implements IntelliJTutorialClientObject {

  private final Topic<T> topic;
  private MessageBusConnection connection;

  protected IntelliJMessageBusObserverBase(@NotNull Topic<T> topic, @NotNull TutorialComponent component) {
    super(component);
    this.topic = topic;
  }

  @Override
  public void activate() {
    Optional.ofNullable(getProject()).ifPresent(this::activate);
  }

  private void activate(@NotNull Project project) {
    (connection = project.getMessageBus().connect()).subscribe(topic, getMessageListener());
  }

  protected abstract @NotNull T getMessageListener();

  @Override
  public void deactivate() {
    Optional.ofNullable(connection).ifPresent(MessageBusConnection::disconnect);
    connection = null;
  }
}
