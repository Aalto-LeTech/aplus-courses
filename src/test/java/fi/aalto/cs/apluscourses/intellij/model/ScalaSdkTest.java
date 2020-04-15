package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.testFramework.HeavyPlatformTestCase;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScalaSdkTest extends HeavyPlatformTestCase {

  @Test
  public void testCreateTempFile() throws IOException {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-5.5.5", aPlusProject, 1);

    //  when
    File tempFile = scalaSdk.createTempFile();
    String absolutePath = tempFile.getAbsolutePath();

    //  then
    assertTrue("File is right", absolutePath.contains("/tmp/unitTest_createTempFile"));
    assertTrue("File is right", absolutePath.contains("/scala-5.5.5.zip"));
  }

  @Test
  public void testFetchZipToWithValidZipWorks() throws IOException {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);
    File testFile = createTempFile("testFile.zip", "");

    //  when
    scalaSdk.fetchZipTo(testFile);

    //  then
    long length = testFile.length() / 1_000_000;
    assertTrue("Size of the loaded Scala stuff is around 20MB", length >= 20);
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testFetchZipToWithInvalidZipThrows() throws IOException {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-5.5.5", aPlusProject, 1);
    File testFile = createTempFile("testFile.zip", "");

    //  when
    try {
      scalaSdk.fetchZipTo(testFile);
    } catch (IOException ex) {
      //  then
      assertEquals("The correct exception is thrown",
          "https://scala-lang.org/files/archive/scala-5.5.5.zip", ex.getMessage());
    }
  }

  @Test
  public void testGetUris(){
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);
    String[] uris = scalaSdk.getUris();
    final String[] ALL_CLASSES = {
        "scala-compiler.jar",
        "scala-library.jar",
        "scala-reflect.jar"
    };

    assertEquals("Two elements are present", 2, uris.length);
    assertSameElements(uris, ALL_CLASSES);
  }

  @Test
  public void testLoadInternal() {
    //  given
    Project project = getProject();
    APlusProject aPlusProject = new APlusProject(project);
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);

    //  when
    // there are dependencies on this, so doing via implementing class
    Runnable r = scalaSdk::loadInternal;
    WriteCommandAction.runWriteCommandAction(project, r);

    // then
    LibraryTable libraryTable = aPlusProject.getLibraryTable();
    Library[] libraries = libraryTable.getLibraries();
    Arrays.stream(libraries).forEach(System.out::println);
  }
}