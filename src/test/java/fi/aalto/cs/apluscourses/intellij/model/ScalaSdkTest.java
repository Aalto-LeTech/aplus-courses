package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.HeavyPlatformTestCase;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

public class ScalaSdkTest extends HeavyPlatformTestCase {

  //todo before each or some other common setup

  @Test
  public void testExtractZip() throws IOException {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);
    File zip = new File("src/test/resources/scala-2.12.10.zip");
    String commonPath = "/tmp/unitTest_extractZip/lib/scala-sdk-2.12.10/";

    //  when
    scalaSdk.extractZip(zip);

    //  then
    String testName = getTestName(true);
    VirtualFile scalaLibrary = LocalFileSystem.getInstance().findFileByIoFile(
        new File(commonPath + "scala-library.jar"));
    assertNotNull("The extracted library scala library exists.", scalaLibrary);
    VirtualFile scalaCompiler = LocalFileSystem.getInstance().findFileByIoFile(
        new File(commonPath + "scala-compiler.jar"));
    assertNotNull("The extracted library scala compiler exists.", scalaCompiler);
    VirtualFile scalaReflect = LocalFileSystem.getInstance().findFileByIoFile(
        new File(commonPath + "scala-reflect.jar"));
    assertNotNull("The extracted library scala reflect exists.", scalaReflect);
  }

  @Test
  public void testCreateTempFile() throws IOException {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-5.5.5", aPlusProject, 1);

    //  when
    File tempFile = scalaSdk.createTempFile();
    String absolutePath = tempFile.getAbsolutePath();

    //  then
    assertTrue("Created file path part is correctFile is right.",
        absolutePath.contains("/tmp/unitTest_createTempFile"));
    assertTrue("Created file path part is correctFile is right.",
        absolutePath.contains("/scala-5.5.5.zip"));
  }

  @Test
  public void testFetchZipToWithValidZipWorks() throws IOException {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);
    File testFile = createTempFile("testFile.zip", "");
    File zip = new File("src/test/resources/scala-2.12.10.zip");

    //  when
    //  this might occasionally fail as it actually fetches data from the network
    scalaSdk.fetchZipTo(testFile);

    //  then
    assertEquals(zip.length(), testFile.length());
    long length = testFile.length() / 1_000_000;
    assertTrue("Size of the loaded Scala stuff is around 20MB.", length >= 20);
  }

  @Test
  public void testFileName() {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);

    //  when
    String fileName = scalaSdk.getFileName();

    //  then
    assertEquals("The correct file name is returned.", "scala-2.12.10", fileName);
  }

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
  public void testGetUrisNoArguments() {
    //  given
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);

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
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);
    final String[] ALL_CLASSES = {
        "scala-compiler.jar",
        "scala-library.jar",
        "scala-reflect.jar"
    };

    //  when
    String[] uris = scalaSdk.getUris(ALL_CLASSES);

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
    APlusProject aPlusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aPlusProject, 1);
    final String[] ALL_CLASSES = {
        "scala-compiler.jar",
        "",
        "scala-reflect.jar"
    };

    //  when
    String[] uris = scalaSdk.getUris(ALL_CLASSES);

    // then
    assertEquals("Two elements are present", 2, uris.length);
    assertTrue("Contains first required .jar",
        uris[0].contains("/lib/scala-sdk-2.12.10/scala-compiler.jar"));
    assertTrue("Contains second required .jar",
        uris[1].contains("/lib/scala-sdk-2.12.10/scala-reflect.jar"));
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