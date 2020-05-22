package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.util.io.FileUtilRt;
import fi.aalto.cs.apluscourses.utils.DomUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SettingsImporterImplTest {

  @Test
  public void testCreateCustomWorkspaceXml() throws IOException {
    File temp = FileUtilRt.createTempFile("xml", "xml", true);
    FileUtils.writeStringToFile(temp, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<project version=\"4\"></project>", StandardCharsets.UTF_8);
    Document document = SettingsImporterImpl.createCustomWorkspaceXml(temp.toPath());
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

}
