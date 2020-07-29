package icons;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

/**
 * Guide: https://www.jetbrains.org/intellij/sdk/docs/reference_guide/work_with_icons_and_images.html
 * Icons source: https://jetbrains.design/intellij/resources/icons_list/
 * Accent colour: #FF0090(FF) (100% opacity)
 * Light schema: #AFB1B3(FF) (100% opacity)
 * Dark schema: #6E6E6E(FF) (100% opacity)
 */
public interface PluginIcons {

  Icon A_PLUS_MODULE = IconLoader.getIcon("/META-INF/icons/module.svg");
  Icon A_PLUS_EXERCISE_GROUP = IconLoader.getIcon("/META-INF/icons/exerciseGroup.svg");
  Icon A_PLUS_NO_SUBMISSIONS = IconLoader.getIcon("/META-INF/icons/noSubmissions.svg");
  Icon A_PLUS_NO_POINTS = IconLoader.getIcon("/META-INF/icons/noPoints.svg");
  Icon A_PLUS_PARTIAL_POINTS = IconLoader.getIcon("/META-INF/icons/partialPoints.svg");
  Icon A_PLUS_FULL_POINTS = IconLoader.getIcon("/META-INF/icons/fullPoints.svg");
  Icon A_PLUS_DOWNLOAD = IconLoader.getIcon("/META-INF/icons/download.svg");
  Icon A_PLUS_UPLOAD = IconLoader.getIcon("/META-INF/icons/upload.svg");
  Icon A_PLUS_BROWSE = IconLoader.getIcon("/META-INF/icons/web.svg");
  Icon A_PLUS_REFRESH = IconLoader.getIcon("/META-INF/icons/refresh.svg");
  Icon A_PLUS_LOGO = IconLoader.getIcon("/META-INF/icons/aPlusLogo.svg");
}
