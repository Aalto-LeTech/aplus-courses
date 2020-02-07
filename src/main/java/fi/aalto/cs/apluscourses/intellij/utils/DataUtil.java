package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataUtil {
  public static <T> DataContext extendDataContext(@NotNull DataKey<T> dataKey,
                                                  @Nullable T value,
                                                  @Nullable DataContext baseDataContext) {
    return new ExtendedDataContext(dataKey, value, baseDataContext);
  }

  public static DataContext extendDataContext(@NotNull Project project,
                                              @Nullable DataContext baseDataContext) {
    return extendDataContext(CommonDataKeys.PROJECT, project, baseDataContext);
  }

  private DataUtil() {

  }

  private static class ExtendedDataContext implements DataContext {
    @NotNull
    private final DataContext baseDataContext;
    @NotNull
    private final String dataId;
    @Nullable
    private final Object value;

    public <T> ExtendedDataContext(@NotNull DataKey<T> dataKey,
                                   @Nullable T value,
                                   @Nullable DataContext baseDataContext) {
      dataId = dataKey.getName();
      this.value = value;
      this.baseDataContext = baseDataContext == null ? DataContext.EMPTY_CONTEXT : baseDataContext;
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
      return dataId.equals(this.dataId) ? value : baseDataContext.getData(dataId);
    }
  }
}
