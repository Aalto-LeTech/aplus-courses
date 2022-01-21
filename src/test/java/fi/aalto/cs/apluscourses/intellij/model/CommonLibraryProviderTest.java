package fi.aalto.cs.apluscourses.intellij.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.NoSuchComponentException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommonLibraryProviderTest {

  @Test
  void testGetScalaSdk() throws NoSuchComponentException {
    String name = "scala-sdk-2.13.1";
    APlusProject project = mock(APlusProject.class);

    CommonLibraryProvider libraryProvider = new CommonLibraryProvider(project);

    Component scalaSdk = libraryProvider.getComponent(name);
    Component scalaSdk2 = libraryProvider.getComponent(name);

    MatcherAssert.assertThat("Returned library must be a ScalaSdk.", scalaSdk, instanceOf(ScalaSdk.class));
    Assertions.assertEquals(name, scalaSdk.getName(), "Name must be the one that was given.");
    Assertions.assertSame(scalaSdk, scalaSdk2, "Sequential calls should return the same object.");
  }

  @Test
  void testGetUnknownLibrary() {
    APlusProject project = mock(APlusProject.class);

    CommonLibraryProvider libraryProvider = new CommonLibraryProvider(project);

    Component library = libraryProvider.getComponentIfExists("unknown");

    Assertions.assertNull(library);
  }

  @Test
  void testGetUnknownLibraryThrows() throws NoSuchComponentException {
    APlusProject project = mock(APlusProject.class);

    CommonLibraryProvider libraryProvider = new CommonLibraryProvider(project);

    assertThrows(NoSuchComponentException.class, () ->
        libraryProvider.getComponent("unknown"));
  }
}
