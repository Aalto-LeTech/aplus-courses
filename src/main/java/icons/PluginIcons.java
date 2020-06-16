package icons;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

/**
 * Guide: https://www.jetbrains.org/intellij/sdk/docs/reference_guide/work_with_icons_and_images.html
 * Icons source: https://jetbrains.design/intellij/resources/icons_list/
 * Accent colour: #FF0090 (100% opacity)
 */
public interface PluginIcons {

  Icon A_PLUS_MODULE = IconLoader.getIcon("/META-INF/icons/module.svg");
  Icon A_PLUS_DOWNLOAD = IconLoader.getIcon("/META-INF/icons/download.svg");
  Icon A_PLUS_UPLOAD = IconLoader.getIcon("/META-INF/icons/upload.svg");
  Icon A_PLUS_REFRESH = IconLoader.getIcon("/META-INF/icons/refresh.svg");
  Icon A_PLUS_LOGO = IconLoader.getIcon("/META-INF/icons/aPlusLogo.svg");
}
