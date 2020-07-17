package fi.aalto.cs.apluscourses.presentation;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.utils.Event;
import org.jetbrains.annotations.NotNull;

public class AuthenticationViewModel extends BaseViewModel<Authentication> {

  public final Event changed = new Event();

  public AuthenticationViewModel(@NotNull Authentication authentication) {
    super(authentication);
  }

  public int getMaxLength() {
    return getModel().maxTokenLength();
  }

  public void setToken(@NotNull char[] token) {
    getModel().setToken(token);
    onChanged();
  }

  @Override
  public void onChanged() {
    changed.trigger();
  }

  public boolean isSet() {
    return getModel().isSet();
  }
}
