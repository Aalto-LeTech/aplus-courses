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
    //{
    //    "id": 2,
    //    "url": "http://localhost:8000/api/v2/courses/1/groups/2/",
    //    "members": [
    //        {
    //            "id": 500,
    //            "url": "http://localhost:8000/api/v2/users/500/",
    //            "username": "student2",
    //            "student_id": "307518",
    //            "email": "student2@example.org",
    //            "full_name": "Ben Cauliflower",
    //            "is_external": false
    //        },
    //        {
    //            "id": 501,
    //            "url": "http://localhost:8000/api/v2/users/501/",
    //            "username": "student3",
    //            "student_id": "135846",
    //            "email": "student3@example.org",
    //            "full_name": "Ben Bond",
    //            "is_external": false
    //        }
    //    ],
    //    "timestamp": "2020-06-09T10:41:30.250085+03:00"
    //}

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