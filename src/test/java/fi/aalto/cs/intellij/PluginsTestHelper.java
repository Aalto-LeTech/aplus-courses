package fi.aalto.cs.intellij;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

public class PluginsTestHelper extends BasePlatformTestCase {

  @NotNull
  public List<IdeaPluginDescriptor> getDummyPluginsListOfTwo() {
    String[] paths = {"src/test/resources/plugins/dummy_a+_plugin.xml",
        "src/test/resources/plugins/dummy_scala_plugin.xml"};
    return getDummyPluginsListOfTwo(paths);
  }

  @NotNull
  public List<IdeaPluginDescriptor> getDummyPluginsListOfTwo(@NotNull String[] paths) {
    return Arrays.stream(paths).map(path -> {
      try {
        return getIdeaPluginDescriptor(path);
      } catch (IOException | JDOMException e) {
        e.printStackTrace();
        return null;
      }
    }).collect(Collectors.toList());
  }

  @NotNull
  public IdeaPluginDescriptorImpl getIdeaPluginDescriptor(@NotNull String path)
      throws IOException, JDOMException {
    File filePath = new File(path);
    IdeaPluginDescriptorImpl ideaPluginDescriptor =
        new IdeaPluginDescriptorImpl(filePath, false);
    ideaPluginDescriptor.loadFromFile(filePath, null, true);
    return ideaPluginDescriptor;
  }

}
