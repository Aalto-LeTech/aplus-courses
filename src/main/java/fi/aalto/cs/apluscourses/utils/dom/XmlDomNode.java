package fi.aalto.cs.apluscourses.utils.dom;

import fi.aalto.cs.apluscourses.utils.DomUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlDomNode implements Node {
  private final @NotNull Element element;

  public static @NotNull XmlDomNode read(@NotNull InputStream stream)
      throws ParserConfigurationException, IOException, SAXException {
    return new XmlDomNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream).getDocumentElement());
  }

  private XmlDomNode(@NotNull Element element) {
    this.element = element;
  }

  @Override
  public @NotNull String getKey() {
    return element.getTagName();
  }

  @Override
  public @NotNull Stream<@NotNull Node> streamChildren() {
    return DomUtil.streamElements(element.getChildNodes()).map(XmlDomNode::new);
  }

  @Override
  public @NotNull String getContent() {
    return element.getTextContent();
  }

  @Override
  public @Nullable String optProp(@NotNull String key) {
    return element.hasAttribute(key) ? element.getAttribute(key) : null;
  }
}
