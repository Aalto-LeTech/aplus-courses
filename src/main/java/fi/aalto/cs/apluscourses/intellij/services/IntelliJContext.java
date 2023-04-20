package fi.aalto.cs.apluscourses.intellij.services;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.intellij.notifications.MessageNotification;
import fi.aalto.cs.apluscourses.intellij.notifications.NotificationUtil;
import fi.aalto.cs.apluscourses.model.Exercise;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.base.Message;
import fi.aalto.cs.apluscourses.presentation.base.PresentationContext;
import fi.aalto.cs.apluscourses.presentation.base.SelectableNodeViewModel;
import fi.aalto.cs.apluscourses.ui.base.IntelliJDialog;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJContext extends PresentationContext {
  private static final String KEY_PROJECT = "intellij.project";
  private static final String KEY_VIEWS = "intellij.views";

  public static final IntelliJContext DEFAULT = new IntelliJContext();

  private IntelliJContext() {

  }

  protected IntelliJContext(@NotNull PresentationContext context) {
    super(context);
  }

  public @NotNull IntelliJContext withProject(@Nullable Project project) {
    var context = new IntelliJContext(this);
    context.setProject(project);
    return context;
  }

  public @Nullable Project getProject() {
    return (Project) get(KEY_PROJECT);
  }

  public void setProject(@Nullable Project project) {
    set(KEY_PROJECT, project);
  }

  private @NotNull IntelliJViews getViews() {
    return (IntelliJViews) getOrSet(KEY_VIEWS, IntelliJViews::new);
  }

  public <T> void registerView(@NotNull Class<T> klass,
                               @NotNull BiFunction<? super @NotNull T,
                                                   @NotNull PresentationContext,
                                                   @NotNull JComponent> viewFactory) {
    getViews().register(klass, viewFactory::apply);
  }

  public @NotNull JComponent createViewFor(@NotNull Object viewModel) {
    return getViews().call(viewModel, this);
  }

  @Override
  public boolean presentModal(@NotNull Object viewModel) {
    return new IntelliJDialog(viewModel, this).showAndGet();
  }

  @Override
  public void showMessage(@NotNull Message message) {
    Notifications.Bus.notify(new MessageNotification(message), getProject());
  }

  @Override
  public void showMessageAndHide(@NotNull Message message) {
    // Notifications.Bus.notifyAndHide(Notification, Project) is still part of the experimental API
    NotificationUtil.notifyAndHide(new MessageNotification(message), getProject());
  }

  @Override
  public @Nullable Exercise getSelectedExercise() {
    return Optional.of(PluginSettings.getInstance().getMainViewModel(getProject()))
        .map(MainViewModel::getExercises)
        .map(BaseTreeViewModel::getSelectedItem)
        .map(SelectableNodeViewModel::getModel)
        .filter(Exercise.class::isInstance)
        .map(Exercise.class::cast)
        .orElse(null);
  }
}
