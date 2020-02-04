package fi.aalto.cs.intellij.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
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
    documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

  }

  private static XPathExpression getCachedXPathExpression(String xPath) {
    return xPathCache.computeIfAbsent(xPath, DomUtil::compileXPath);
  }

  @NotNull
  private static XPathExpression compileXPath(String xPath) {
    try {
      return xPathFactory.newXPath().compile(xPath);
    } catch (XPathExpressionException e) {
      return die("Bad XPath", e);
    }
  }

  private static <T> T die(String message, Exception e) {
    logger.error(message, e);
    throw new Error(message, e);
  }

  private DomUtil() {

  }

  public static List<Node> getNodeListFromXPath(String xPath, Node node) {
    try {
      NodeList nodeList = (NodeList) getCachedXPathExpression(xPath).evaluate(node,
          XPathConstants.NODESET);
      List<Node> result = new ArrayList<>();
      for (int i = 0, length = nodeList.getLength(); i < length; i++) {
        result.add(nodeList.item(i));
      }
      return result;
    } catch (XPathExpressionException e) {
      return die("XPathExpression failed", e);
    }
  }

  public static Document parse(File file) throws DomException {
    try {
      return documentBuilderFactory.newDocumentBuilder().parse(file);
    } catch (SAXException | ParserConfigurationException | IOException e) {
      throw new DomException(e);
    }
  }
}
