package fi.aalto.cs.intellij.util;

import org.junit.Assert;
import org.junit.Test;

public class StringSplitterTest {

  @Test
  public void testStringSplitter() {
    StringSplitter splitter = new StringSplitter("John,,Paul,Ringo Starr,George,", ',');

    Assert.assertFalse(splitter.finished());
    Assert.assertEquals("John", splitter.readNext());
    Assert.assertFalse(splitter.finished());
    Assert.assertEquals("", splitter.readNext());
    Assert.assertEquals("Paul", splitter.readNext());
    Assert.assertEquals("Ringo Starr", splitter.readNext());
    Assert.assertEquals("George", splitter.readNext());
    Assert.assertFalse(splitter.finished());
    Assert.assertEquals("", splitter.readNext());
    Assert.assertTrue(splitter.finished());

    try {
      splitter.readNext();
    } catch (IllegalStateException ex) {
      return;
    }

    Assert.fail();
  }

  @Test
  public void testStringSplitterReadInt() {
    StringSplitter splitter = new StringSplitter("zero.1.2.3.4,5", '.');

    Assert.assertEquals("zero", splitter.readNext());
    Assert.assertEquals("1", splitter.readNext());
    Assert.assertEquals(2, splitter.readNextInt());
    Assert.assertEquals(3, splitter.readNextInt());
    Assert.assertFalse(splitter.finished());

    try {
      splitter.readNextInt();
    } catch (NumberFormatException ex) {
      return;
    }

    Assert.fail();
  }

  @Test
  public void testStringSplitterEmpty() {
    StringSplitter splitter = new StringSplitter("", ' ');
    Assert.assertFalse(splitter.finished());
    Assert.assertEquals("", splitter.readNext());
    Assert.assertTrue(splitter.finished());
  }
}
