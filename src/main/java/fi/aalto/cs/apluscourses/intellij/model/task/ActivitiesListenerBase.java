package fi.aalto.cs.apluscourses.intellij.model.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.NonBlockingReadAction;
import com.intellij.openapi.application.ReadAction;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import fi.aalto.cs.apluscourses.model.task.ActivitiesListener;
import fi.aalto.cs.apluscourses.model.task.ListenerCallback;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.CalledInAny;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for IDE activities listeners.
 * Subclasses should override abstract methods and,
 * if need be, getDefaultParameter() and prepareReadAction() but no other methods.
 * Subclasses should call check() when they observe a change.
 * Subclasses should not call check() before registerListenerOverride() is called.
 * The parameter given to check() is passed to checkOverride().
 * Please make sure the parameter is thread safe!
 * Initially, check() is called with the default parameter.
 */
public abstract class ActivitiesListenerBase<T> implements ActivitiesListener {

  private static final Executor EDT_EXECUTOR = ApplicationManager.getApplication()::invokeLater;
  private static final Executor BG_EXECUTOR = AppExecutorUtil.getAppExecutorService();

  private static final int INITIAL = 0;
  private static final int REGISTERED = 1;
  private static final int UNREGISTERED = 2;

  private final ListenerCallback callback;
  private final AtomicBoolean isAlreadyFinished = new AtomicBoolean(false);
  private final AtomicInteger state = new AtomicInteger(INITIAL);

  protected ActivitiesListenerBase(ListenerCallback callback) {
    this.callback = callback;
  }

  @CalledInAny
  @Override
  public void registerListener() {
    check(this::getDefaultParameter, this::handleInitialResult);
  }

  @CalledInAny
  @Override
  public void unregisterListener() {
    EDT_EXECUTOR.execute(this::unregisterListenerInternal);
  }

  private void registerListenerInternal() {
    if (state.compareAndSet(INITIAL, REGISTERED)) {
      registerListenerOverride();
    }
  }

  private void unregisterListenerInternal() {
    if (state.compareAndSet(REGISTERED, UNREGISTERED)) {
      unregisterListenerOverride();
    }
  }

  @CalledInAny
  protected void check(T param) {
    check(() -> param, this::handleSubsequentResult);
  }

  @CalledInAny
  private void check(@RequiresReadLock Supplier<T> paramSupplier,
                     @RequiresEdt Consumer<Boolean> resultHandler) {
    prepareReadAction(ReadAction.nonBlocking(() -> checkOverride(paramSupplier.get())))
        .finishOnUiThread(ModalityState.NON_MODAL, resultHandler)
        .submit(BG_EXECUTOR);
  }

  @RequiresEdt
  private void handleInitialResult(boolean isSuccess) {
    if (isSuccess) {
      callback.onHappened(true);
    } else {
      callback.onStarted();
      registerListenerInternal();
    }
  }

  @RequiresEdt
  private void handleSubsequentResult(boolean isSuccess) {
    if (isSuccess && !isAlreadyFinished.getAndSet(true)) {
      callback.onHappened(false);
    }
  }

  @RequiresReadLock
  protected abstract boolean checkOverride(T param);

  @RequiresEdt
  protected abstract void registerListenerOverride();

  @RequiresEdt
  protected abstract void unregisterListenerOverride();

  @CalledInAny
  protected <V> NonBlockingReadAction<V> prepareReadAction(@NotNull NonBlockingReadAction<V> action) {
    return action;
  }

  @RequiresReadLock
  protected T getDefaultParameter() {
    return null;
  }
}
