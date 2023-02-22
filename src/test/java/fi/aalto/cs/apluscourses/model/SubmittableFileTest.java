package fi.aalto.cs.apluscourses.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubmittableFileTest {

  @Test
  void testCreate() {
    String key = "mykey";
    String name = "myname";
    SubmittableFile submittableFile = new SubmittableFile(key, name);
    Assertions.assertEquals(key, submittableFile.getKey());
    Assertions.assertEquals(name, submittableFile.getName());
  }

}
