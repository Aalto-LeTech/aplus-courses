package fi.aalto.cs.apluscourses.intellij.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProjectKeyTest {

  @Test
  void testProjectKey() {
    var project1 = mock(Project.class);
    var project2 = mock(Project.class);
    var project3 = mock(Project.class);
    doReturn("abc/def").when(project1).getBasePath();
    doReturn("abc/def").when(project2).getBasePath();
    doReturn("ghi/jkl").when(project3).getBasePath();
    var key1 = new ProjectKey(project1);
    var key2 = new ProjectKey(project2);
    var key3 = new ProjectKey(project3);

    Assertions.assertEquals(key1, key2);
    Assertions.assertEquals(key1.hashCode(), key2.hashCode());
    Assertions.assertNotEquals(key1, key3);
    Assertions.assertNotEquals(key2, key3);
  }

  @Test
  void testProjectKeyWithDefaultProject() {
    var project1 = mock(Project.class);
    doReturn(true).when(project1).isDefault();
    var project2 = mock(Project.class);
    doReturn("").when(project2).getBasePath();
    var key1 = new ProjectKey(project1);
    var key2 = new ProjectKey(project2);

    Assertions.assertEquals(key1, key2);
    Assertions.assertEquals(key1.hashCode(), key2.hashCode());
  }

}
