package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.utils.Cookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAuthentication implements Authentication {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthentication.class);
  private final Set<Cookie> cookies = Collections.synchronizedSet(new HashSet<>());

  @Override
  public void addToRequest(HttpRequest request) {
    URI uri = URI.create(request.getRequestLine().getUri());
    String header = String.join(";",
        Set.copyOf(cookies)
            .stream()
            .filter(cookie -> cookie.matchUri(uri))
            .map(Cookie::toString)
            .map(CharSequence.class::cast)
            ::iterator);
    LOGGER.debug(header);
    request.addHeader("Cookie", header);
  }

  @Override
  public boolean persist() {
    return false;
  }

  @Override
  public void clear() {

  }

  public void setCookie(Cookie cookie) {
    LOGGER.debug("setCookie: {}", cookie);
    synchronized (cookies) {
      cookies.remove(cookie);
      cookies.add(cookie);
    }
  }

  @Override
  public void handleResponse(HttpResponse response) {
    for (Header header : response.getHeaders("Set-Cookie")) {
      // apparently header.getElements() is useless here
      String[] elements = header.getValue().split(";");
      Cookie cookie = new Cookie();
      cookie.name = getAttr(elements[0]);
      cookie.value = getVal(elements[0]);
      for (int i = 1; i < elements.length; i++) {
        String attr = getAttr(elements[i]);
        String val = getVal(elements[i]);
        switch (attr) {
          case "Path":
            cookie.path = val;
            break;
          case "Domain":
            cookie.domain = val;
            break;
          //TODO: Expires, etc.
        }
      }
      setCookie(cookie);
    }
  }

  private String getAttr(String element) {
    int index = element.indexOf("=");
    return index >= 0 ? element.substring(0, index) : element;
  }

  private String getVal(String element) {
    int index = element.indexOf("=");
    return index >= 0 ? element.substring(index) : null;
  }
}
