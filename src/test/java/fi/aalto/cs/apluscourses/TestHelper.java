package fi.aalto.cs.apluscourses;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.presentation.ReplConfigurationFormModel;
import fi.aalto.cs.apluscourses.ui.repl.ReplConfigurationForm;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

/**
 * A helper interface< to simplify testing plugin manipulation logics.
 */
public interface TestHelper {

  /**
   * A helper method that creates a sample {@link List} of {@link IdeaPluginDescriptor} based on the
   * test data.
   *
   * @return a {@link List} of two valid {@link IdeaPluginDescriptor}s.
   */
  @NotNull
  static List<IdeaPluginDescriptor> getDummyPluginsListOfTwo() {
    String[] paths = {"src/test/resources/plugins/dummy_a+_plugin.xml",
        "src/test/resources/plugins/dummy_liferay_plugin.xml"};
    return getDummyPluginsListOfTwo(paths);
  }

  /**
   * A helper method that creates a sample {@link List} of {@link IdeaPluginDescriptor} based on the
   * provided data.
   *
   * @param paths an array of {@link String} pointing to plugin.xml files of plugins to load.
   * @return a {@link List} of two valid {@link IdeaPluginDescriptor}s.
   */
  @NotNull
  static List<IdeaPluginDescriptor> getDummyPluginsListOfTwo(@NotNull String[] paths) {
    return Arrays.stream(paths).map(path -> {
      try {
        return getIdeaPluginDescriptor(path);
      } catch (IOException | JDOMException ex) {
        ex.printStackTrace();
        return null;
      }
    }).collect(Collectors.toList());
  }

  /**
   * A helper method that creates a sample {@link IdeaPluginDescriptor} from the plugin.xml file.
   * provided data.
   *
   * @param path a {@link String} pointing to plugin.xml file of plugins to load.
   * @return a {@link List} of two valid {@link IdeaPluginDescriptor}s.
   */
  @NotNull
  static IdeaPluginDescriptorImpl getIdeaPluginDescriptor(@NotNull String path)
      throws IOException, JDOMException {
    File file = new File(path);
    IdeaPluginDescriptorImpl ideaPluginDescriptor =
        new IdeaPluginDescriptorImpl(file.toPath(), false);
    ideaPluginDescriptor.loadFromFile(file, null, true);
    return ideaPluginDescriptor;
  }

  /**
   * A helper method that creates a sample {@link IdeaPluginDescriptor} for "IDEA CORE" plugin.
   *
   * @return a valid {@link IdeaPluginDescriptor} for "IDEA CORE" plugin.
   */
  @NotNull
  static IdeaPluginDescriptor getIdeaCorePluginDescriptor() {
    return Objects.requireNonNull(PluginManager.getPlugin(PluginId.getId("com.intellij")));
  }

  /**
   * A helper method to ensure at least one (first) module for the default testing project is of a
   * Scala type.
   *
   * @param project a default testing {@link Project}
   */
  static void makeFirstPluginScalaModule(@NotNull Project project) {
    Module[] modules = ModuleManager.getInstance(project).getModules();
    makeFirstPluginScalaModule(modules);
  }

  /**
   * A helper method to ensure at least one (first) module for the default testing project is of a
   * Scala type.
   *
   * @param modules an array of {@link Module}s
   */
  static void makeFirstPluginScalaModule(@NotNull Module[] modules) {
    if (modules[0] != null) {
      Module module = modules[0];
      module.setModuleType("JAVA_MODULE");
    }
  }

  /**
   * A helper method to create a dummy {@link ReplConfigurationFormModel} from default project.
   *
   * @return the created {@link ReplConfigurationFormModel}
   */
  @NotNull
  default ReplConfigurationFormModel getDummyReplConfigurationFormModel(@NotNull Project project) {
    String workDir = project.getProjectFilePath();
    String moduleName = "light_idea_test_case";
    makeFirstPluginScalaModule(project);
    return new ReplConfigurationFormModel(project, workDir, moduleName);
  }

  /**
   * A helper method to create a dummy {@link ReplConfigurationForm} from default project.
   *
   * @return the created {@link ReplConfigurationForm}
   */
  @NotNull
  default ReplConfigurationForm getDummyReplConfigurationForm(@NotNull Project project) {
    return new ReplConfigurationForm(getDummyReplConfigurationFormModel(project));
  }

  /**
   * Helper method for {@link com.intellij.testFramework.HeavyPlatformTestCase} to add modules.
   *
   * @param project a {@link Project} to install {@link Module}s into
   * @param name {@link String} name of the added {@link Module}
   * @param moduleTypeId {@link String} id of the added {@link Module}
   */
  default void createAndAddModule(Project project, String name, String moduleTypeId) {
    ModuleManager moduleManager = ModuleManager.getInstance(project);
    Runnable r = () -> moduleManager.newModule(name, moduleTypeId);
    WriteCommandAction.runWriteCommandAction(project, r);
  }
}
