package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import org.apache.http.HttpRequest;

public class SessionAuthentication implements Authentication {

  private final String sessionid;

  public SessionAuthentication(String sessionid) {
    this.sessionid = sessionid;
  }

  @Override
  public void addToRequest(HttpRequest request) {
    request.addHeader("Cookie", "sessionid=" + sessionid);
  }

  @Override
  public boolean persist() {
    return false;
  }

  @Override
  public void clear() {

  }
}
