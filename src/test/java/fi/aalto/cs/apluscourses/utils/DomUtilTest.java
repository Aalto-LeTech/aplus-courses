package fi.aalto.cs.apluscourses.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.intellij.openapi.util.io.FileUtilRt;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DomUtilTest {

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testGetNodes() throws IOException, SAXException {
    List<Node> nodes = DomUtil.getNodesFromXPath("/root/outer/inner",
        DomUtilTest.class.getClassLoader().getResourceAsStream("dom-util-test.xml"));

    assertEquals("There should be three nodes",
        3, nodes.size());
    assertEquals("The text content of the first node should be 'foo'",
        "foo", nodes.get(0).getTextContent());
    assertEquals("The text content of the second node should be 'bar'",
        "bar", nodes.get(1).getTextContent());
    assertEquals("The text content of the third node should be 'baz'",
        "baz", nodes.get(2).getTextContent());
  }

  @Test
  public void testWriteDocumentToFile() throws IOException, SAXException {
    File temp = FileUtilRt.createTempFile("test", "xml", true);
    FileUtils.writeStringToFile(temp, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root />",
        StandardCharsets.UTF_8);

    Document document = DomUtil.parse(temp);
    document.getDocumentElement().appendChild(document.createElement("child"));

    DomUtil.writeDocumentToFile(document, temp);

    document = DomUtil.parse(temp);

    List<Node> nodes = DomUtil.getNodesFromXPath("//child", document);
    assertFalse("The file should contain the correct XML", nodes.isEmpty());
  }
}
