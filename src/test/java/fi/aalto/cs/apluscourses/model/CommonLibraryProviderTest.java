package fi.aalto.cs.apluscourses.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.aalto.cs.apluscourses.intellij.model.APlusProject;
import fi.aalto.cs.apluscourses.intellij.model.CommonLibraryProvider;
import fi.aalto.cs.apluscourses.intellij.model.ScalaSdk;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class CommonLibraryProviderTest {

  @Test
  public void testGetScalaSdk() throws NoSuchComponentException {
    String name = "scala-sdk-2.13.1";
    APlusProject project = mock(APlusProject.class);
    when(project.resolveLibraryState(any())).thenReturn(Component.NOT_INSTALLED);

    CommonLibraryProvider libraryProvider = new CommonLibraryProvider(project);

    Component scalaSdk = libraryProvider.getComponent(name);
    Component scalaSdk2 = libraryProvider.getComponent(name);

    assertThat("Returned library must be a ScalaSdk.", scalaSdk, instanceOf(ScalaSdk.class));
    assertEquals("Name must be the one that was given.", name, scalaSdk.getName());
    assertSame("Sequential calls should return the same object.", scalaSdk, scalaSdk2);
  }

  @Test
  public void testGetUnknownLibrary() {
    APlusProject project = mock(APlusProject.class);

    CommonLibraryProvider libraryProvider = new CommonLibraryProvider(project);

    Component library = libraryProvider.getComponentIfExists("unknown");

    assertNull(library);
  }

  @Test(expected = NoSuchComponentException.class)
  public void testGetUnknownLibraryThrows() throws NoSuchComponentException {
    APlusProject project = mock(APlusProject.class);

    CommonLibraryProvider libraryProvider = new CommonLibraryProvider(project);

    Component library = libraryProvider.getComponent("unknown");
  }
}
