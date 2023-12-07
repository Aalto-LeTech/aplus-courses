package fi.aalto.cs.apluscourses.intellij.dal;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BackgroundThreadPasswordStorage implements PasswordStorage {
  private final @NotNull Executor edtExecutor = ApplicationManager.getApplication()::invokeLater;
  private final @NotNull Executor bgExecutor = ApplicationManager.getApplication()::executeOnPooledThread;

  @Override
  public void store(@NotNull String user,
                    char @Nullable [] password,
                    @RequiresEdt @NotNull Runnable onSuccess,
                    @RequiresEdt @NotNull Runnable onFailure) {
    bgExecutor.execute(() -> edtExecutor.execute(storeInternal(user, password) ? onSuccess : onFailure));
  }

  @Override
  public void remove() {
    bgExecutor.execute(this::removeInternal);
  }

  @Override
  public void restorePassword(@RequiresEdt @NotNull Consumer<char @NotNull[]> onSuccess,
                              @RequiresEdt @NotNull Runnable onFailure) {
    bgExecutor.execute(() -> {
      var password = restorePasswordInternal();
      edtExecutor.execute(password == null ? onFailure : () -> onSuccess.accept(password));
    });
  }

  @RequiresBackgroundThread
  protected abstract void removeInternal();

  @RequiresBackgroundThread
  protected abstract boolean storeInternal(@NotNull String user, char @Nullable [] password);

  @RequiresBackgroundThread
  public abstract char @Nullable [] restorePasswordInternal();
}
