package fi.aalto.cs.apluscourses.model;

import java.util.Arrays;
import java.util.Collections;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class GroupTest {

  @Test
  public void testGroup() {
    Group group = new Group(123, Arrays.asList("John", "Catherine"));
    Assert.assertEquals("The ID should be equal to the one given to the constructor",
        123, group.getId());
    Assert.assertEquals("The member names should be equal to those given to the constructor",
        "John", group.getMemberNames().get(0));
    Assert.assertEquals("The member names should be equal to those given to the constructor",
        "Catherine", group.getMemberNames().get(1));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetMemberNamesReturnsUnmodifiableList() {
    new Group(0, Collections.emptyList()).getMemberNames().add("");
  }

  @Test
  public void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put("id", 145)
        .put("members", new JSONArray()
            .put(new JSONObject().put("full_name", "Erkki"))
            .put(new JSONObject().put("full_name", "Henkka")));

    Group group = Group.fromJsonObject(json);

    Assert.assertEquals("The ID should be equal to the one in the JSON object",
        145, group.getId());
    Assert.assertEquals("The member names should be equal to those in the JSON object",
        "Erkki", group.getMemberNames().get(0));
    Assert.assertEquals("The member names should be equal to those in the JSON object",
        "Henkka", group.getMemberNames().get(1));
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingId() {
    JSONObject json = new JSONObject().put("members", new JSONArray());
    Group.fromJsonObject(json);
  }

  @Test(expected = JSONException.class)
  public void testFromJsonObjectMissingMembers() {
    JSONObject json = new JSONObject().put("id", 0);
    Group.fromJsonObject(json);
  }

}
