package fi.aalto.cs.apluscourses.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DomUtil {

  private static Logger logger = LoggerFactory.getLogger(DomUtil.class);

  private static ConcurrentMap<String, XPathExpression> xPathCache;
  private static final XPathFactory xPathFactory;
  private static final DocumentBuilderFactory documentBuilderFactory;

  static {
    xPathCache = new ConcurrentHashMap<>();

    xPathFactory = XPathFactory.newInstance();

    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    } catch (IllegalArgumentException e) {
      logger.warn("Could not set XXE restrictions for DOM tools because the platform does not "
          + "support them.");
    }
  }

  @NotNull
  private static XPathExpression getCachedXPathExpression(@NotNull String xpath) {
    return xPathCache.computeIfAbsent(xpath, DomUtil::compileXPath);
  }

  @NotNull
  private static XPathExpression compileXPath(@NotNull String xpath) {
    try {
      return xPathFactory.newXPath().compile(xpath);
    } catch (XPathExpressionException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private DomUtil() {

  }

  /**
   * Get a list of nodes that matches the given XPath when applied to the given node.
   * @param xpath A {@link String} that contains an XPath expression.
   * @param node  A {@link Node} that is used as a root for XPath.
   * @return A {@link List} containing all the {@link Node}s that match the XPath.
   * @throws IllegalArgumentException If the given string is not a valid XPath.
   */
  @NotNull
  public static List<Node> getNodesFromXPath(@NotNull String xpath, @NotNull Node node) {
    NodeList nodeList;
    try {
      nodeList = (NodeList) getCachedXPathExpression(xpath).evaluate(node, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new IllegalArgumentException(e);
    }
    return CommonUtil.createList(nodeList.getLength(), nodeList::item);
  }
  
  @NotNull
  public static List<Node> getNodesFromXPath(@NotNull String xpath, @NotNull File file)
      throws IOException, SAXException {
    return getNodesFromXPath(xpath, parse(file));
  }

  /**
   * Parses the given file to a DOM document.
   * @param file A {@link File}.
   * @return A DOM {@link Document}.
   * @throws IOException  If the file cannot be accessed.
   * @throws SAXException If the content of the file is not properly structured.
   */
  @NotNull
  public static Document parse(@NotNull File file) throws IOException, SAXException {
    try {
      return documentBuilderFactory.newDocumentBuilder().parse(file);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException();
    }
  }
}
