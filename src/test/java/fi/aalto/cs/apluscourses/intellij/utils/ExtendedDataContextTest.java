package fi.aalto.cs.apluscourses.intellij.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import org.junit.Test;

public class ExtendedDataContextTest {

  @Test
  public void testExtendDataContext() {
    DataContext baseDataContext = mock(DataContext.class);

    String dataId1 = "keyThatExistsInBaseDataContext";
    Object value1 = new Object();
    doReturn(value1).when(baseDataContext).getData(dataId1);

    String dataId2 = "keyThatExistsInBothDataContexts";
    Object value21 = new Object();
    doReturn(value21).when(baseDataContext).getData(dataId2);


    String dataId3 = "keyThatExistsInExtendedDataContext";
    Object value3 = new Object();
    Object value22 = new Object();
    Object value23 = new Object();
    DataContext extendedDataContext = new ExtendedDataContext(baseDataContext)
        .with(dataId2, value22)
        .with(dataId2, value23)
        .with(dataId3, value3);

    assertEquals("The value should come from the base data context",
        value1, extendedDataContext.getData(dataId1));
    assertEquals("The value should be overridden",
        value23, extendedDataContext.getData(dataId2));
    assertEquals("The value should come from the extended data context",
        value3, extendedDataContext.getData(dataId3));
  }

  @Test
  public void testExtendedDataContextWithDataKey() {
    String dataId = "newDataKey";
    Object value = new Object();
    DataContext dataContext = new ExtendedDataContext().with(DataKey.create(dataId), value);

    assertEquals("The value should be set", value, dataContext.getData(dataId));
  }

  @Test
  public void testExtendedDataContextWithProject() {
    Project project = mock(Project.class);
    DataContext dataContext = new ExtendedDataContext().withProject(project);

    assertSame("The project should be set", project, dataContext.getData(CommonDataKeys.PROJECT));
  }
}
