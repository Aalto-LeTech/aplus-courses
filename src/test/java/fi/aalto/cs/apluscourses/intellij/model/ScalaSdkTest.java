package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.testFramework.HeavyPlatformTestCase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class ScalaSdkTest extends HeavyPlatformTestCase {

  @Test
  public void testCreateTempFile() throws IOException {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-5.5.5", aplusProject);

    //  when
    File tempFile = scalaSdk.createTempFile();
    Path absolutePath = tempFile.toPath();

    //  then
    assertTrue("Created file path part is correct.", absolutePath.endsWith("scala-5.5.5.zip"));
  }

  @Test
  public void testFileName() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject);

    //  when
    String fileName = scalaSdk.getFileName();

    //  then
    assertEquals("The correct file name is returned.", "scala-2.12.10", fileName);
  }

  @Test
  public void testGetUrisNoArguments() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject);

    //  when
    String[] uris = scalaSdk.getUris();

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
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject);
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
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject);
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
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject);
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
