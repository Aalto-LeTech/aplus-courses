package fi.aalto.cs.apluscourses.presentation.commands;

import fi.aalto.cs.apluscourses.dal.APlusTokenAuthentication;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.presentation.AuthenticationViewModel;
import fi.aalto.cs.apluscourses.presentation.MainViewModel;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.dialogs.Dialogs;
import fi.aalto.cs.apluscourses.presentation.messages.ApiTokenNotSetMessage;
import fi.aalto.cs.apluscourses.presentation.messages.Messenger;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class APlusAuthenticationCommand implements Command<MainViewModelContext> {

  @NotNull
  private final PasswordStorage.Factory passwordStorageFactory;

  public APlusAuthenticationCommand(@NotNull PasswordStorage.Factory passwordStorageFactory) {
    this.passwordStorageFactory = passwordStorageFactory;
  }

  @Override
  public boolean canExecute(@NotNull MainViewModelContext context) {
    return canAuthenticationBeSet(context.getMainViewModel());
  }

  @Override
  public void execute(@NotNull MainViewModelContext context) {
    setAuthentication(context.getMainViewModel(), context.getDialogs(), context.getMessenger());
  }

  private boolean canAuthenticationBeSet(@NotNull MainViewModel mainViewModel) {
    return mainViewModel.courseViewModel.get() != null;
  }

  private void setAuthentication(@NotNull MainViewModel mainViewModel,
                                 @NotNull Dialogs dialogs,
                                 @NotNull Messenger messenger) {
    Course course = Optional.ofNullable(mainViewModel.courseViewModel.get())
        .map(BaseViewModel::getModel)
        .orElse(null);

    if (course == null) {
      return;
    }

    String apiUrl = course.getApiUrl();
    PasswordStorage passwordStorage = passwordStorageFactory.create(apiUrl);
    AuthenticationViewModel authenticationViewModel =
        new AuthenticationViewModel(APlusTokenAuthentication.getFactoryFor(passwordStorage));

    if (!dialogs.create(authenticationViewModel).showAndGet()) {
      return;
    }

    Authentication authentication = authenticationViewModel.build();
    if (!authentication.persist()) {
      messenger.show(new ApiTokenNotSetMessage());
    }
    mainViewModel.disposing.addListener(authentication, Authentication::clear);
    Optional.ofNullable(mainViewModel.authentication.getAndSet(authentication))
        .ifPresent(Authentication::clear);
  }
}
