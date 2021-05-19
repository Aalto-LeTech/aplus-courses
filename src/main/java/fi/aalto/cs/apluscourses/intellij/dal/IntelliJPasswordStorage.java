package fi.aalto.cs.apluscourses.intellij.dal;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJPasswordStorage implements PasswordStorage {

  public static final String A_COURSES_PLUGIN = "A+ Courses Plugin";

  @NotNull
  private final CredentialAttributes credentialAttributes;

  @NotNull
  private final PasswordSafe passwordSafe;

  public IntelliJPasswordStorage(@NotNull String service) {
    this(service, PasswordSafe.getInstance());
  }

  /**
   * Constructs a new instance.
   *
   * @param service The name of the service (URL) that the username/password is for.
   * @param passwordSafe A {@link PasswordSafe} that stores username/password.
   */
  public IntelliJPasswordStorage(@NotNull String service,
                                 @NotNull PasswordSafe passwordSafe) {
    this.credentialAttributes = new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, service));
    this.passwordSafe = passwordSafe;
  }

  @Override
  public boolean store(@NotNull String user, char @Nullable [] password) {
    Credentials credentials = password == null ? null : new Credentials(user, password);
    passwordSafe.set(credentialAttributes, credentials);
    return !passwordSafe.isPasswordStoredOnlyInMemory(credentialAttributes, credentials);
  }

  @Override
  public void remove() {
    passwordSafe.set(credentialAttributes, null);
  }

  @Override
  public char[] restorePassword() {
    return Optional.ofNullable(passwordSafe.get(credentialAttributes))
        .map(Credentials::getPassword)
        .map(OneTimeString::toCharArray)
        .orElse(null);
  }
}
