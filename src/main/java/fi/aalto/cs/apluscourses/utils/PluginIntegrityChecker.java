package fi.aalto.cs.apluscourses.utils;

import net.lingala.zip4j.ZipFile;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;
import org.jsoup.safety.Safelist;

public class PluginIntegrityChecker {

  /**
   * Checks if A+ Courses' required dependencies are correctly installed and available.
   */
  public static boolean isPluginCorrectlyInstalled() {
    try {
      return !ZipFile.class.toString().isEmpty() && !JSONObject.class.toString().isEmpty()
          && !Safelist.class.toString().isEmpty() && !HttpClient.class.toString().isEmpty();
    } catch (Exception ignored) {
      // the potential exception here is some kind of ReflectiveOperationException, which
      // could only be caused by a missing class (= missing dependency)
    }

    return false;
  }

  private PluginIntegrityChecker() {

  }
}
