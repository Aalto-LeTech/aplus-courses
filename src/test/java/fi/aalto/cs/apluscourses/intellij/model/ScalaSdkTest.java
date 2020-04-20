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