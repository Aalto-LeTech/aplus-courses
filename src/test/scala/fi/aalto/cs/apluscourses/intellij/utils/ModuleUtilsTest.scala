package fi.aalto.cs.apluscourses.intellij.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import org.junit.Assert.{assertFalse, assertEquals}
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.when

class ModuleUtilsTest {
  @Test
  def testGetModuleDirectory(): Unit = {
    val module = mock(classOf[Module])
    when(module.getModuleFilePath).thenReturn("test/path/module.iml")
    assertEquals("getModuleDirectory returns the correct path", "test/path",
      ModuleUtils.getModuleDirectory(module))
  }

  val nullDataContext = new DataContext {
    override def getData(dataId: String) = null
  }

  @Test
  def testGetModuleOfEditorFile(): Unit = {
    assertFalse(ModuleUtils.getModuleOfEditorFile(mock(classOf[Project]), nullDataContext).isDefined)
  }

  def testGetModuleOfSelectedFile(): Unit = {
    assertFalse(ModuleUtils.getModuleOfEditorFile(mock(classOf[Project]), nullDataContext).isDefined)
  }
}
