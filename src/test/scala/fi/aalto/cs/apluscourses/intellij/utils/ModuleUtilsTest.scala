package fi.aalto.cs.apluscourses.intellij.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{OrderEntry, OrderEnumerator}
import com.intellij.util.Processor
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}

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

  @Test
  def testGetModuleOfSelectedFile(): Unit = {
    assertFalse(ModuleUtils.getModuleOfEditorFile(mock(classOf[Project]), nullDataContext).isDefined)
  }

  @Test
  def testNonEmptyWithEmptyOrderEnumerator(): Unit = {
    val emptyOrderEnumerator = mock(classOf[OrderEnumerator])
    when(
      emptyOrderEnumerator.forEach(
        any(classOf[Processor[OrderEntry]])
      )
    ).thenAnswer(_ => { })
    assertFalse(ModuleUtils.nonEmpty(emptyOrderEnumerator))
  }

  @Test
  def testNonEmptyWithNonEmptyOrderEnumerator(): Unit = {
    val orderEntry = mock(classOf[OrderEntry])
    val orderEnumerator = mock(classOf[OrderEnumerator])
    when(
      orderEnumerator.forEach(
        any(classOf[Processor[OrderEntry]])
      )
    ).thenAnswer(mockInvocation => {
      val processor = mockInvocation.getArgument[Processor[OrderEntry]](0)
      processor.process(orderEntry)
    })
    assertTrue(ModuleUtils.nonEmpty(orderEnumerator))
  }
}
