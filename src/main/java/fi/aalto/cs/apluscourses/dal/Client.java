package fi.aalto.cs.apluscourses.dal;

import fi.aalto.cs.apluscourses.model.Authentication;
import java.io.IOException;
import java.util.Map;
import org.json.JSONObject;

public interface Client {
  JSONObject fetch(String url, Authentication authentication) throws IOException;

  String post(String url,
              Authentication authentication,
              Map<String, Object> data) throws IOException;
}
