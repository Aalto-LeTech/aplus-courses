package fi.aalto.cs.apluscourses.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.BalloonImpl;
import fi.aalto.cs.apluscourses.utils.Cast;
import fi.aalto.cs.apluscourses.utils.CollectionUtil;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntelliJUIUtil {

  public static @Nullable BalloonImpl getBalloon(@Nullable Project project) {
    return getBalloon(WindowManager.getInstance().getFrame(project));
  }

  public static @Nullable BalloonImpl getBalloon(@Nullable JFrame rootFrame) {
    return rootFrame == null
        ? null
        : CollectionUtil.ofType(JPanel.class, Arrays.stream(rootFrame.getLayeredPane().getComponents()))
            .map(IntelliJUIUtil::getBalloon)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
  }

  public static @Nullable BalloonImpl getBalloon(@NotNull JPanel panel) {
    return Optional.ofNullable(panel.getClientProperty(Balloon.KEY))
        .map(Cast.to(BalloonImpl.class)::orNull)
        .orElse(null);
  }
}
