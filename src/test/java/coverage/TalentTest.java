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
public class TalentTest {

  @Inject
  Logger log;

  final String talentName = "Test Talent";
  final String newName = "New Talent Name";

  String exampleTalentJson() {
    return new JSONObject()
      .put("firstname", talentName)
      .put("lastname", talentName)
      .put("address", "Main St")
      .put("city", "Baton Rouge")
      .put("state", "LA")
      .put("zip", "70113")
      .toString();
  }

  @Test
  public void testTalentsEndpoint() {
    given().when().get("/talent").then().statusCode(200);
  }

  @Test
  public void testTalentAddAndDeleteAll() {
    String a = exampleTalentJson();

    given().when().delete("/talent").then().statusCode(200);

    String addedTalentURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/talent")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    given()
      .when()
      .get(addedTalentURI)
      .then()
      .statusCode(200)
      .body("firstname", equalTo(talentName));

    given().when().delete("/talent").then().statusCode(200);
  }

  @Test
  public void getTalent() {
    String a = exampleTalentJson();

    given().when().delete("/talent").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/talent")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/talent")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .get("/talent/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/talent").then().statusCode(200);
  }

  @Test
  public void failOnBadTalentId() {
    given().when().delete("/talent").then().statusCode(200);

    given()
      .pathParam("id", "60d1434f7fe4d40a3c74d8c7")
      .when()
      .get("/talent/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  public void testDeleteTalent() {
    String a = exampleTalentJson();

    given().when().delete("/talent").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/talent")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/talent")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .delete("/talent/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/talent").then().statusCode(200);
  }

  @Test
  public void testUpdateTalent() {
    String a = exampleTalentJson();

    given().when().delete("/talent").then().statusCode(200);

    String addedTalentURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/talent")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    given()
      .when()
      .get(addedTalentURI)
      .then()
      .statusCode(200)
      .body("firstname", equalTo(talentName));

    String r = given()
      .when()
      .get(addedTalentURI)
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
      .put(addedTalentURI)
      .then()
      .statusCode(200);

    given()
      .when()
      .get(addedTalentURI)
      .then()
      .statusCode(200)
      .body("firstname", equalTo(newName));

    given().when().delete("/talent").then().statusCode(200);
  }
}
