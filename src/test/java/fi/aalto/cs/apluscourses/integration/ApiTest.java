package fi.aalto.cs.apluscourses.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;

public class ApiTest {

  //  For this to work the 'CI=true' environment variable is added to .travis.yml
  @ClassRule
  public static final EnvironmentChecker checker = new EnvironmentChecker("CI");

  @Test
  public void getStudentsGroupsReturnsCorrect() {
    given()
        .auth()
        .preemptive()
        .basic("root", "root")
        .when()
        .contentType(ContentType.JSON)
        .get("http://localhost:8000/api/v2/courses/1/groups/2/")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("members.id", hasItems(503, 504))
        .body("members.username", hasItems("student5", "student6"));
  }
}