package icons;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.JBColor;
import java.awt.Color;
import javax.swing.Icon;

/**
 * <a href="https://www.jetbrains.org/intellij/sdk/docs/reference_guide/work_with_icons_and_images.html">Guide</a><br>
 * <a href="https://jetbrains.design/intellij/resources/icons_list/">Icons source</a><br>
 * Accent colour: #FF0090(FF)<br>
 * (100% opacity) aka RGB(255, 0, 144) Light schema: #AFB1B3(FF) (100% opacity) Dark schema:
 * #6E6E6E(FF) (100% opacity).
 */
public final class PluginIcons {
  private PluginIcons() {
    throw new IllegalStateException("Utility class");
  }

  public static final int ACCENT_COLOR = new JBColor(
      new Color(255, 0, 144),
      new Color(255, 0, 144)
  ).getRGB();

  public static final Icon A_PLUS_MODULE = IconLoader
      .getIcon("/META-INF/icons/module.svg", PluginIcons.class);
  public static final Icon A_PLUS_EXERCISE_GROUP = IconLoader
      .getIcon("/META-INF/icons/exerciseGroup.svg", PluginIcons.class);
  public static final Icon A_PLUS_EXERCISE_GROUP_CLOSED = IconLoader
      .getIcon("/META-INF/icons/exerciseGroupClosed.svg", PluginIcons.class);
  public static final Icon A_PLUS_OPTIONAL_PRACTICE = IconLoader
      .getIcon("/META-INF/icons/optionalPractice.svg", PluginIcons.class);
  public static final Icon A_PLUS_NO_SUBMISSIONS = IconLoader
      .getIcon("/META-INF/icons/noSubmissions.svg", PluginIcons.class);
  public static final Icon A_PLUS_NO_POINTS = IconLoader
      .getIcon("/META-INF/icons/noPoints.svg", PluginIcons.class);
  public static final Icon A_PLUS_PARTIAL_POINTS = IconLoader
      .getIcon("/META-INF/icons/partialPoints.svg", PluginIcons.class);
  public static final Icon A_PLUS_FULL_POINTS = IconLoader
      .getIcon("/META-INF/icons/fullPoints.svg", PluginIcons.class);
  public static final Icon A_PLUS_LATE = IconLoader
      .getIcon("/META-INF/icons/late.svg", PluginIcons.class);
  public static final Icon A_PLUS_DOWNLOAD = IconLoader
      .getIcon("/META-INF/icons/download.svg", PluginIcons.class);
  public static final Icon A_PLUS_UPLOAD = IconLoader
      .getIcon("/META-INF/icons/upload.svg", PluginIcons.class);
  public static final Icon A_PLUS_BROWSE = IconLoader
      .getIcon("/META-INF/icons/web.svg", PluginIcons.class);
  public static final Icon A_PLUS_FEEDBACK = IconLoader
      .getIcon("/META-INF/icons/feedback.svg", PluginIcons.class);
  public static final Icon A_PLUS_REFRESH = IconLoader
      .getIcon("/META-INF/icons/refresh.svg", PluginIcons.class);
  public static final Icon A_PLUS_LOGO = IconLoader
      .getIcon("/META-INF/icons/aPlusLogo.svg", PluginIcons.class);
  public static final Icon A_PLUS_LOGO_COLOR = IconLoader
      .getIcon("/META-INF/icons/aPlusLogoColor.svg", PluginIcons.class);
  public static final Icon A_PLUS_REPL = IconLoader
      .getIcon("/META-INF/icons/repl.svg", PluginIcons.class);
  public static final Icon A_PLUS_FILTER = IconLoader
      .getIcon("/META-INF/icons/filter.svg", PluginIcons.class);
  public static final Icon A_PLUS_COURSES_BANNER = IconLoader
      .getIcon("/META-INF/images/courses-banner.png", PluginIcons.class);
  public static final Icon A_PLUS_COURSES_FOOTER = IconLoader
      .getIcon("/META-INF/images/footer.png", PluginIcons.class);
  public static final AnimatedIcon A_PLUS_IN_GRADING = new AnimatedIcon.Default();
  public static final Icon A_PLUS_USER = IconLoader
      .getIcon("/META-INF/icons/user.svg", PluginIcons.class);
  public static final Icon A_PLUS_USER_ACTIVE = IconLoader
      .getIcon("/META-INF/icons/user_pink.svg", PluginIcons.class);
  public static final Icon A_PLUS_INFO = IconLoader
      .getIcon("/META-INF/icons/info.svg", PluginIcons.class);
  public static final Icon A_PLUS_DUMMY = IconLoader
      .getIcon("/META-INF/icons/dummy.svg", PluginIcons.class);
  public static final Icon A_PLUS_CHECKED = IconLoader
      .getIcon("/META-INF/icons/checked.svg", PluginIcons.class);
  public static final Icon A_PLUS_DOCS = IconLoader
      .getIcon("/META-INF/icons/docs.svg", PluginIcons.class);
  public static final Icon A_PLUS_PLUS = AllIcons.General.Add;
}
