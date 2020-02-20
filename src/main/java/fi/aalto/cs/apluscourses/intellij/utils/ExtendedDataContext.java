package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExtendedDataContext implements DataContext {
  @NotNull
  private final DataContext baseDataContext;
  @Nullable
  private final String dataId;
  @Nullable
  private final Object value;

  /**
   * Constructs an {@link ExtendedDataContext} based on an empty context.
   */
  public ExtendedDataContext() {
    this.dataId = null;
    this.value = null;
    this.baseDataContext = DataContext.EMPTY_CONTEXT;
  }

  /**
   * Constructs an {@link ExtendedDataContext} based on the given {@link DataContext}.
   * @param baseDataContext A {@link DataContext} on which the {@link ExtendedDataContext} is based.
   */
  public ExtendedDataContext(@NotNull DataContext baseDataContext) {
    this.dataId = null;
    this.value = null;
    this.baseDataContext = baseDataContext;
  }

  private <T> ExtendedDataContext(@NotNull String dataId,
                                  @Nullable T value,
                                  @NotNull DataContext baseDataContext) {
    this.dataId = dataId;
    this.value = value;
    this.baseDataContext = baseDataContext;
  }

  @Nullable
  @Override
  public Object getData(@NotNull String dataId) {
    return dataId.equals(this.dataId) ? value : baseDataContext.getData(dataId);
  }

  public <T> ExtendedDataContext with(@NotNull String dataId, @Nullable T value) {
    return new ExtendedDataContext(dataId, value, this);
  }

  public <T> ExtendedDataContext with(@NotNull DataKey<T> dataId, @Nullable T value) {
    return with(dataId.getName(), value);
  }

  public ExtendedDataContext withProject(@NotNull Project project) {
    return with(CommonDataKeys.PROJECT, project);
  }
}
