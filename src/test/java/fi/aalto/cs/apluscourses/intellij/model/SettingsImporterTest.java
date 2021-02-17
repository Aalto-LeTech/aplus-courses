package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.utils.DomUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SettingsImporterTest {

  @Test
  public void testCreateCustomWorkspaceXml() throws IOException {
    File temp = FileUtilRt.createTempFile("xml", "xml", true);
    FileUtils.writeStringToFile(temp, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<project version=\"4\"></project>", StandardCharsets.UTF_8);
    Document document = SettingsImporter.createCustomWorkspaceXml(temp.toPath());
    List<Node> componentMatches = DomUtil.getNodesFromXPath(
        "//component[@name=\"CompilerWorkspaceConfiguration\"]", document);
    Assert.assertFalse("The customized workspace.xml should contain the correct component node",
        componentMatches.isEmpty());
    List<Node> optionMatches = DomUtil.getNodesFromXPath(
        "//option[@name=\"AUTO_SHOW_ERRORS_IN_EDITOR\" and @value=\"false\"]",
        componentMatches.get(0));
    Assert.assertFalse("The component node should contain the correct setting",
        optionMatches.isEmpty());
  }

  @Test
  public void testCreateCustomWorkspaceXmlWithExistingComponent() throws IOException {
    File temp = FileUtilRt.createTempFile("xml", "xml", true);
    FileUtils.writeStringToFile(temp, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<project version=\"4\"><component name=\"CompilerWorkspaceConfiguration\"><option name="
        + "\"AUTO_SHOW_ERRORS_IN_EDITOR\" value=\"true\"/></component></project>",
        StandardCharsets.UTF_8);
    Document document = SettingsImporter.createCustomWorkspaceXml(temp.toPath());
    List<Node> componentMatches = DomUtil.getNodesFromXPath(
        "//component[@name=\"CompilerWorkspaceConfiguration\"]", document);
    Assert.assertFalse("The customized workspace.xml should contain the correct component node",
        componentMatches.isEmpty());
    List<Node> optionMatches = DomUtil.getNodesFromXPath(
        "//option[@name=\"AUTO_SHOW_ERRORS_IN_EDITOR\" and @value=\"false\"]",
        componentMatches.get(0));
    Assert.assertFalse("The component node should contain the correct setting",
        optionMatches.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testCreateCustomWorkspaceXmlWithMalformedXml() throws IOException {
    File temp = FileUtilRt.createTempFile("malformed", "xml", true);
    FileUtils.writeStringToFile(temp, "<?xml version\"1.0\" encoding=\"UTF-8\"?><component>",
        StandardCharsets.UTF_8);
    SettingsImporter.createCustomWorkspaceXml(temp.toPath());
  }

}
