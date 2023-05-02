package fi.aalto.cs.apluscourses.intellij.model.tutorial.util;

import com.intellij.xdebugger.XSourcePosition;
import fi.aalto.cs.apluscourses.model.tutorial.CodeContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XDebuggerUtil {
  public static boolean containsSourcePosition(@NotNull CodeContext codeContext, @Nullable XSourcePosition position) {
    return position != null && codeContext.contains(position.getFile().toNioPath(), position.getOffset());
  }
}
