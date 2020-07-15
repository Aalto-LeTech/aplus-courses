package fi.aalto.cs.apluscourses.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
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
  public void testGetStudentsGroups() {
    given()
        .auth()
        .preemptive()
        .basic("root", "root")
        .when()
        .contentType(ContentType.JSON)
        .get("http://localhost:8000/api/v2/courses/100/groups/200/")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("members.id", hasItems(500, 501))
        .body("members.username", hasItems("percash0", "zoralst1"));
  }

  @Test
  public void testGetExercises() {
    given()
        .auth()
        .preemptive()
        .basic("root", "root")
        .when()
        .contentType(ContentType.JSON)
        .get("http://localhost:8000/api/v2/courses/100/exercises/")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("results.display_name", hasItems("1. First module", "1. Second module"))
        .body("results.exercises.id[0]", hasItems(300, 301))
        .body("results.exercises.id[1]", hasItems(302));
  }

  @Test
  public void testGetIndividualExercise() {
    given()
        .auth()
        .preemptive()
        .basic("root", "root")
        .when()
        .contentType(ContentType.JSON)
        .get("http://localhost:8000/api/v2/exercises/301/")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("exercise_info", equalTo(null))
        .body("max_submissions", equalTo(5));
  }

  @Test
  public void testGetSubmissions() {
    given()
        .auth()
        .preemptive()
        .basic("root", "root")
        .when()
        .contentType(ContentType.JSON)
        .get("http://localhost:8000/api/v2/exercises/301/submissions/me/")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("count", equalTo(0));
  }
}
