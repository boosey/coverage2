package coverage;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EngagementTest {

  @Inject
  Logger log;

  final String name = "Test Engagement";
  final String newName = "New Engagement Name";

  String exampleEngagementJson() {
    return new JSONObject()
      .put("name", name)
      .put("description", "Engagement description")
      .toString();
  }

  @Test
  public void testEngagementsEndpoint() {
    given().when().get("/engagements").then().statusCode(200);
  }

  @Test
  public void testEngagementAddAndDeleteAll() {
    String a = exampleEngagementJson();

    given().when().delete("/engagements").then().statusCode(200);

    String addedEngagementURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/engagements")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    given()
      .when()
      .get(addedEngagementURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(name));

    given().when().delete("/engagements").then().statusCode(200);
  }

  @Test
  public void getEngagement() {
    String a = exampleEngagementJson();

    given().when().delete("/engagements").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/engagements")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/engagements")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .get("/engagements/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/engagements").then().statusCode(200);
  }

  @Test
  public void failOnBadEngagementId() {
    given().when().delete("/engagements").then().statusCode(200);

    given()
      .pathParam("id", "60d1434f7fe4d40a3c74d8c7")
      .when()
      .get("/engagements/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  public void testDeleteEngagement() {
    String a = exampleEngagementJson();

    given().when().delete("/engagements").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/engagements")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/engagements")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .delete("/engagements/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/engagements").then().statusCode(200);
  }

  @Test
  public void testUpdateEngagement() {
    String a = exampleEngagementJson();

    given().when().delete("/engagements").then().statusCode(200);

    String addedEngagementURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/engagements")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    given()
      .when()
      .get(addedEngagementURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(name));

    String r = given()
      .when()
      .get(addedEngagementURI)
      .then()
      .statusCode(200)
      .extract()
      .body()
      .asString();

    JSONObject j = new JSONObject(r);
    j.put("name", newName);
    String a1 = j.toString();

    given()
      .contentType(ContentType.JSON)
      .body(a1)
      .when()
      .put(addedEngagementURI)
      .then()
      .statusCode(200);

    given()
      .when()
      .get(addedEngagementURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(newName));

    given().when().delete("/engagements").then().statusCode(200);
  }
}
