package fi.aalto.cs.apluscourses.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
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

  private static final String BASE_URL = "http://localhost:8000/api/v2/";

  @Test
  public void getSubmissionsResultsReturns() {
    final String firstExercise = "modules[0].exercises[0]";
    final String url = BASE_URL + "courses/100/points/me/";

    given()
        .auth()
        .preemptive()
        .basic("zoralst1", "zoralst1")
        .when()
        .contentType(ContentType.JSON)
        .get(url)
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("modules[0].id", equalTo(10))
        .body(firstExercise + ".id", equalTo(300))
        .body(firstExercise + ".max_points", equalTo(100))
        .body(firstExercise + ".points_to_pass", equalTo(50))
        .body(firstExercise + ".submission_count", equalTo(3))
        .body(firstExercise + ".points", equalTo(60))
        .body(firstExercise + ".passed", equalTo(true))
        .body(firstExercise + ".submissions[0]", containsString("402"))
        .body(firstExercise + ".submissions[1]", containsString("401"))
        .body(firstExercise + ".submissions[2]", containsString("400"));
  }

  @Test
  public void testGetStudentsGroups() {
    given()
        .auth()
        .preemptive()
        .basic("zoralst1", "zoralst1")
        .when()
        .contentType(ContentType.JSON)
        .get(BASE_URL + "courses/100/mygroups/")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .body("results[0].id", equalTo(200))
        .body("results[0].members.id", hasItems(5, 6))
        .body("results[0].members.full_name", hasItems("Perry Cash", "Zorita Alston"));
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
