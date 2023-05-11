package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.ui.popup.Balloon;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJUIUtil {
  public static @Nullable JPanel getBalloonPanel(JFrame rootFrame) {
    return CollectionUtil.ofType(JPanel.class, Arrays.stream(rootFrame.getLayeredPane().getComponents()))
        .filter(IntelliJUIUtil::isBalloonPanel)
        .findAny()
        .orElse(null);
  }

  public static boolean isBalloonPanel(@NotNull JPanel panel) {
    return panel.getClientProperty(Balloon.KEY) != null;
  }
}
