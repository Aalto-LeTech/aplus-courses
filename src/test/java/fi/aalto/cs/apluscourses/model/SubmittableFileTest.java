package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class SubmittableFileTest {

  @Test
  public void testCreate() {
    String key = "mykey";
    String name = "myname";
    SubmittableFile submittableFile = new SubmittableFile(key, name);
    assertEquals(key, submittableFile.getKey());
    assertEquals(name, submittableFile.getName());
  }

}
