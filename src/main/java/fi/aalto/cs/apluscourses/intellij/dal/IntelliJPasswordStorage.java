package fi.aalto.cs.apluscourses.intellij.dal;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJPasswordStorage extends BackgroundThreadPasswordStorage {

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
   * @param service      The name of the service (URL) that the username/password is for.
   * @param passwordSafe A {@link PasswordSafe} that stores username/password.
   */
  public IntelliJPasswordStorage(@NotNull String service,
                                 @NotNull PasswordSafe passwordSafe) {
    this.credentialAttributes = new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, service));
    this.passwordSafe = passwordSafe;
  }

  @Override
  @RequiresBackgroundThread
  public boolean storeInternal(@NotNull String user, char @Nullable [] password) {
    Credentials credentials = password == null ? null : new Credentials(user, password);
    passwordSafe.set(credentialAttributes, credentials);
    return !passwordSafe.isPasswordStoredOnlyInMemory(credentialAttributes, credentials);
  }

  @Override
  @RequiresBackgroundThread
  public void removeInternal() {
    passwordSafe.set(credentialAttributes, null);
  }

  @Override
  @RequiresBackgroundThread
  public char[] restorePasswordInternal() {
    return Optional.ofNullable(passwordSafe.get(credentialAttributes))
        .map(Credentials::getPassword)
        .map(OneTimeString::toCharArray)
        .orElse(null);
  }
}
