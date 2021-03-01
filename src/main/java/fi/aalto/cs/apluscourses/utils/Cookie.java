package fi.aalto.cs.apluscourses.utils;

import java.net.URI;

public class Cookie {
  public String name;
  public String value;
  public String domain;
  public String path;

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Cookie)) {
      return false;
    }
    Cookie other = (Cookie) obj;
    return domain.equals(other.domain) && name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return domain.hashCode() ^ name.hashCode();
  }

  @Override
  public String toString() {
    return name + "=" + value;
  }

  public boolean matchUri(URI uri) {
    return domain.equals(uri.getHost()) && uri.getPath().startsWith(getSlashEndingPath());
  }

  public String getSlashEndingPath() {
    return path == null ? "/" : path.endsWith("/") ? path : path + "/";
  }
}
