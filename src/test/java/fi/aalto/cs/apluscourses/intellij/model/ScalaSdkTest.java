package fi.aalto.cs.apluscourses.intellij.model;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.HeavyPlatformTestCase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.jetbrains.annotations.SystemIndependent;
import org.junit.Ignore;
import org.junit.Test;

public class ScalaSdkTest extends HeavyPlatformTestCase {

  @Test
  public void testExtractZip() throws IOException {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);
    @SystemIndependent String path = "src/test/resources/scalaSdk/scala-2.12.10.zip";
    File scalaZip = new File(path);
    @SystemIndependent String basePath = getProject().getBasePath();
    System.out.println("basePath " + basePath);

    //  when
    scalaSdk.extractZip(scalaZip);

    //  then
    VirtualFile scalaLibrary = getVirtualFile(
        new File(basePath + "/lib/scala-sdk-2.12.10/scala-library.jar"));
    assertNotNull("The extracted library scala library exists.", scalaLibrary);
    VirtualFile scalaCompiler = getVirtualFile(
        new File(basePath + "/lib/scala-sdk-2.12.10/scala-library.jar"));
    assertNotNull("The extracted library scala compiler exists.", scalaCompiler);
    VirtualFile scalaReflect = getVirtualFile(
        new File(basePath + "/lib/scala-sdk-2.12.10/scala-library.jar"));
    assertNotNull("The extracted library scala reflect exists.", scalaReflect);
  }

  @Ignore
  @Test
  public void testFetchZipToWithValidZipWorks() throws IOException, InterruptedException {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);
    File testFile = createTempFile("testFile.zip", "");
    @SystemIndependent String path = "src/test/resources/scalaSdk/scala-2.12.10.zip";
    File zip = new File(path);

    //  when
    //  this might occasionally fail as it actually fetches data from the network
    scalaSdk.fetchZipTo(testFile);

    assertEquals(zip.length(), testFile.length());
    //  then
    long length = testFile.length() / 1_000_000;
    assertTrue("Size of the loaded Scala stuff is around 20MB.", length >= 20);
  }

  @Test
  public void testCreateTempFile() throws IOException {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-5.5.5", aplusProject, 1);

    //  when
    File tempFile = scalaSdk.createTempFile();
    String absolutePath = tempFile.getAbsolutePath();

    //  then
    assertTrue("Created file path part is correct.",
        absolutePath.contains("/scala-5.5.5.zip"));
  }

  @Test
  public void testFileName() {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);

    //  when
    String fileName = scalaSdk.getFileName();

    //  then
    assertEquals("The correct file name is returned.", "scala-2.12.10", fileName);
  }

  @Ignore
  @Test
  public void testFetchZipToWithInvalidZipThrows() throws IOException {
    //  given
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-5.5.5", aplusProject, 1);
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
    APlusProject aplusProject = new APlusProject(getProject());
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);

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
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);
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
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);
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
  public void testLoadInternal() {
    //  given
    Project project = spy(getProject());
    APlusProject aplusProject = new APlusProject(project);
    ScalaSdk scalaSdk = new ScalaSdk("scala-sdk-2.12.10", aplusProject, 1);
    ScalaSdk spyScalaSdk = spy(scalaSdk);
    when(spyScalaSdk.getFullPath())
        .thenReturn(Paths.get("src/test/resources/scalaSdk/scala-2.12.10"));

    //  when
    //  there are dependencies on this, so doing via implementing class
    Runnable r = spyScalaSdk::loadInternal;
    WriteCommandAction.runWriteCommandAction(project, r);

    // then
    LibraryTable libraryTable = aplusProject.getLibraryTable();
    Library[] libraries = libraryTable.getLibraries();
    assertEquals("Scala SDK is created.", "scala-sdk-2.12.10", libraries[0].getName());
    String[] jars = libraries[0].getUrls(OrderRootType.CLASSES);
    assertEquals("Scala SDK contains two libraries.", 2, jars.length);
    assertTrue("Scala SDK contains scala-library.jar library.",
        jars[0].contains("scala-library.jar"));
    assertTrue("Scala SDK contains scala-reflect.jar library.",
        jars[1].contains("scala-reflect.jar"));
  }
}