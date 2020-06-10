package fi.aalto.cs.apluscourses.model;

import org.junit.Assert;
import org.junit.Test;

public class APlusAuthenticationTest {

  @Test
  public void testAPlusAuthentication() {
    char[] token = new char[]{'a', 'b', 'c'};
    APlusAuthentication authentication = new APlusAuthentication(token);
    Assert.assertArrayEquals("The token should be equal to the one provided to the constructor",
        token, authentication.getToken());
  }

  @Test
  public void testMakesCopyOfArray() {
    char[] token = new char[]{'d', 'e', 'f'};
    APlusAuthentication authentication = new APlusAuthentication(token);
    token[0] = 'g';
    Assert.assertEquals("The constructor makes a copy of the given array",
        'd', authentication.getToken()[0]);
  }

  @Test
  public void testClear() {
    char[] token = new char[]{'g', 'h', 'i'};
    APlusAuthentication authentication = new APlusAuthentication(token);
    authentication.clear();
    Assert.assertArrayEquals("The token is cleared", new char[]{'\0', '\0', '\0'},
        authentication.getToken());
  }

}
