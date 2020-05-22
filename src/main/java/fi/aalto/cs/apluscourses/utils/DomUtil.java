package fi.aalto.cs.apluscourses.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    return IntStream
        .range(0, nodeList.getLength())
        .mapToObj(nodeList::item)
        .collect(Collectors.toList());
  }

  @NotNull
  public static List<Node> getNodesFromXPath(@NotNull String xpath, @NotNull InputStream stream)
      throws IOException, SAXException {
    return getNodesFromXPath(xpath, parse(stream));
  }

  /**
   * Get a list of nodes that matches the given XPath when applied to the root of the document.
   * @param xpath A {@link String} that contains an XPath expression.
   * @param file  A {@link File} that contains the document.
   * @return A {@link List} containing all the {@link Node}s that match the XPath.
   * @throws IllegalArgumentException If the given string is not a valid XPath.
   */
  @NotNull
  public static List<Node> getNodesFromXPath(@NotNull String xpath, @NotNull File file)
      throws IOException, SAXException {
    try (InputStream stream = new FileInputStream(file)) {
      return getNodesFromXPath(xpath, stream);
    }
  }

  /**
   * Iterates through the immediate children of the given parent and returns the first node with the
   * given name, attribute, and attribute value, or null if such a node isn't found.
   * @param parent         The children of this node are examined.
   * @param tagName        The name of the tag of the child node.
   * @param attributeName  The name of an attribute that the child node must have.
   * @param attributeValue The value corresponding to the attribute name that the child must have.
   * @return The first node with the given tag name, attribute name, and attribute value, or null
   *         if such a node isn't found.
   */
  @Nullable
  public static Node findChildNodeWithAttribute(@NotNull Node parent,
                                                @NotNull String tagName,
                                                @NotNull String attributeName,
                                                @NotNull String attributeValue) {

    List<Node> matches = getNodesFromXPath(
        "//" + tagName + "[@" + attributeName + "=\"" + attributeValue + "\"]", parent);
    if (matches.isEmpty()) {
      return null;
    }
    return matches.get(0);
  }

  /**
   * Parses the content of given input stream to a DOM document.
   * @param stream An {@link InputStream}.
   * @return A DOM {@link Document}.
   * @throws IOException  If the stream cannot be read.
   * @throws SAXException If the content of the stream is not properly structured.
   */
  @NotNull
  public static Document parse(@NotNull InputStream stream) throws IOException, SAXException {
    try {
      return documentBuilderFactory.newDocumentBuilder().parse(stream);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException();
    }
  }

  /**
   * Parses the content of the given file to a DOM document.
   * @param file A {@link File}.
   * @return A DOM {@link Document}.
   * @throws IOException  If the stream cannot be read.
   * @throws SAXException If the content of the stream is not properly structured.
   */
  @NotNull
  public static Document parse(@NotNull File file) throws IOException, SAXException {
    try {
      return documentBuilderFactory.newDocumentBuilder().parse(file);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException();
    }
  }

  /**
   * Writes the given document to the given file.
   * @param document The document that is written to the file.
   * @param out      The file to which the document is written.
   * @throws IOException If an IO error occurs.
   */
  public static void writeDocumentToFile(@NotNull Document document, @NotNull File out)
      throws IOException {
    Source source = new DOMSource(document);
    StreamResult result = new StreamResult(out);
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(source, result);
    } catch (TransformerException ex) {
      throw new IOException(ex);
    }
  }
}
