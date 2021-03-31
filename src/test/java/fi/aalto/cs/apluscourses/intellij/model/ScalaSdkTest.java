package fi.aalto.cs.apluscourses.intellij.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import com.intellij.testFramework.HeavyPlatformTestCase;
import org.junit.Test;

public class ScalaSdkTest extends HeavyPlatformTestCase {


  @Test
  public void testGetClassUris() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", "2.12.10", aplusProject);

    //  when
    String[] uris = scalaSdk.getClassUris();

    // then
    assertEquals("Two elements are present", 2, uris.length);
    assertTrue("Contains first required .jar",
        uris[0].contains("/lib/scala-sdk-2.12.10/scala-library.jar"));
    assertTrue("Contains second required .jar",
        uris[1].contains("/lib/scala-sdk-2.12.10/scala-reflect.jar"));
  }

  @Test
  public void testGetUrisWithValidArguments() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", "2.12.10", aplusProject);
    final String[] allClasses = {
        "scala-compiler.jar",
        "scala-library.jar",
        "scala-reflect.jar"
    };

    //  when
    String[] uris = scalaSdk.getUris(allClasses);

    // then
    assertEquals("Two elements are present", 3, uris.length);
    assertTrue("Contains first required .jar",
        uris[0].contains("/lib/scala-sdk-2.12.10/scala-compiler.jar"));
    assertTrue("Contains second required .jar",
        uris[1].contains("/lib/scala-sdk-2.12.10/scala-library.jar"));
    assertTrue("Contains third required .jar",
        uris[2].contains("/lib/scala-sdk-2.12.10/scala-reflect.jar"));
  }

  @Test
  public void testGetUrisWithInvalidArguments() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", "2.12.10", aplusProject);
    final String[] allClasses = {
        "scala-compiler.jar",
        "",
        "scala-reflect.jar"
    };

    //  when
    String[] uris = scalaSdk.getUris(allClasses);

    // then
    assertEquals("Two elements are present", 2, uris.length);
    assertTrue("Contains first required .jar",
        uris[0].contains("/lib/scala-sdk-2.12.10/scala-compiler.jar"));
    assertTrue("Contains second required .jar",
        uris[1].contains("/lib/scala-sdk-2.12.10/scala-reflect.jar"));
  }

  @Test
  public void testGetUrisWithWeirdCharacters() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", "2.12.10", aplusProject);
    final String[] allClasses = {
        "name with a space.jar",
        "scändinåvian.jör",
        "💩.emoji"
    };

    //  when
    String[] uris = scalaSdk.getUris(allClasses);

    // then
    assertEquals("Three elements are present", 3, uris.length);
    assertThat(uris[0], containsString(allClasses[0]));
    assertThat(uris[1], containsString(allClasses[1]));
    assertThat(uris[2], containsString(allClasses[2]));
  }
}
