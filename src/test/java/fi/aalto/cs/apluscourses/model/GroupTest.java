package fi.aalto.cs.apluscourses.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GroupTest {

  @Test
  void testGroup() {
    Group group = new Group(123, List.of(
        new Group.GroupMember(1, "John"),
        new Group.GroupMember(2, "Catherine")));
    Assertions.assertEquals(123, group.getId(), "The ID should be equal to the one given to the constructor");
    Assertions.assertEquals("John", group.getMemberNames().get(0),
        "The member names should be equal to those given to the constructor");
    Assertions.assertEquals("Catherine", group.getMemberNames().get(1),
        "The member names should be equal to those given to the constructor");
  }

  @Test
  void testGetMemberNamesReturnsUnmodifiableList() {
    assertThrows(UnsupportedOperationException.class, () ->
        new Group(0, Collections.emptyList()).getMemberNames().add(""));
  }

  @Test
  void testFromJsonObject() {
    JSONObject json = new JSONObject()
        .put("id", 145)
        .put("members", new JSONArray()
            .put(new JSONObject().put("full_name", "Erkki"))
            .put(new JSONObject().put("full_name", "Henkka")));

    Group group = Group.fromJsonObject(json);

    Assertions.assertEquals(145, group.getId(), "The ID should be equal to the one in the JSON object");
    Assertions.assertEquals("Erkki", group.getMemberNames().get(0),
        "The member names should be equal to those in the JSON object");
    Assertions.assertEquals("Henkka", group.getMemberNames().get(1),
        "The member names should be equal to those in the JSON object");
  }

  @Test
  void testFromJsonObjectMissingId() {
    JSONObject json = new JSONObject().put("members", new JSONArray());
    assertThrows(JSONException.class, () ->
        Group.fromJsonObject(json));
  }

  @Test
  void testFromJsonObjectMissingMembers() {
    JSONObject json = new JSONObject().put("id", 0);
    assertThrows(JSONException.class, () ->
        Group.fromJsonObject(json));
  }

  @Test
  void testEquals() {
    Group group = new Group(3, new ArrayList<>());
    Group sameGroup = new Group(3, new ArrayList<>());
    Group otherGroup = new Group(5, new ArrayList<>());

    Assertions.assertEquals(group, sameGroup);
    Assertions.assertEquals(group.hashCode(), sameGroup.hashCode());

    Assertions.assertNotEquals(group, otherGroup);
  }
}
