package fi.aalto.cs.apluscourses.intellij.dal;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJPasswordStorage implements PasswordStorage {

  public static final String A_COURSES_PLUGIN = "A+ Courses Plugin";

  @NotNull
  private final CredentialAttributes credentialAttributes;

  public IntelliJPasswordStorage(@NotNull String service) {
    this.credentialAttributes = new CredentialAttributes(
        CredentialAttributesKt.generateServiceName(A_COURSES_PLUGIN, service));
  }

  @Override
  public void store(@NotNull String user, @Nullable char[] password) {
    Credentials credentials = password == null ? null : new Credentials(user, password.clone());
    PasswordSafe.getInstance().set(credentialAttributes, credentials);
  }

  @Override
  @Nullable
  public char[] restorePassword() {
    Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
    if (credentials == null) {
      return null;
    }
    OneTimeString password = credentials.getPassword();
    return password == null ? null : password.toCharArray(true);
  }
}
