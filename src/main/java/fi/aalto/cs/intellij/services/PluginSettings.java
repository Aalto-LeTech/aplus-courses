package fi.aalto.cs.intellij.services;

import com.intellij.openapi.components.ServiceManager;
import fi.aalto.cs.intellij.presentation.MainModel;
import fi.aalto.cs.intellij.utils.DomUtil;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class PluginSettings {

  public static final String COURSE_CONFIGURATION_FILE_PATH = "o1.json";

  @NotNull
  private final MainModel mainModel = new MainModel();

  @NotNull
  public static PluginSettings getInstance() {
    return ServiceManager.getService(PluginSettings.class);
  }

  @NotNull
  public MainModel getMainModel() {
    return mainModel;
  }
}
