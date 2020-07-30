package fi.aalto.cs.apluscourses.intellij.dal;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.CredentialStore;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJPasswordStorage implements PasswordStorage {

  public static final String A_COURSES_PLUGIN = "A+ Courses Plugin";

  @NotNull
  private final CredentialAttributes credentialAttributes;

  @NotNull
  private final CredentialStore credentialStore;

  public IntelliJPasswordStorage(@NotNull String service) {
    this(service, PasswordSafe.getInstance());
  }

  /**
   * Constructs a new instance.
   *
   * @param service The name of the service (URL) that the username/password is for.
   * @param credentialStore A {@link CredentialStore} that stores username/password.
   */
  public IntelliJPasswordStorage(@NotNull String service,
                                 @NotNull CredentialStore credentialStore) {
    this.credentialAttributes = new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, service));
    this.credentialStore = credentialStore;
  }

  @Override
  public boolean store(@NotNull String user, @Nullable char[] password) {
    Credentials credentials = password == null ? null : new Credentials(user, password);
    setInternal(credentials);
    return Objects.equals(credentials, getInternal());
  }

  @Override
  @Nullable
  public char[] restorePassword() {
    return Optional.ofNullable(getInternal())
        .map(Credentials::getPassword)
        .map(OneTimeString::toCharArray)
        .orElse(null);
  }

  @Nullable
  private Credentials getInternal() {
    return credentialStore.get(credentialAttributes);
  }

  private void setInternal(@Nullable Credentials credentials) {
    credentialStore.set(credentialAttributes, credentials);
  }
}
