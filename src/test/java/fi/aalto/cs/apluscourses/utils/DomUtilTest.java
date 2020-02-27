package fi.aalto.cs.apluscourses.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import org.junit.Test;
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
}
