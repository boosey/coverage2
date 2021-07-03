package coverage;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AccountsTest {

  @Inject
  Logger log;

  final String accountName = "Test Account";
  final String newName = "New Account Name";

  String exampleAccountJson() {
    return new JSONObject()
      .put("name", accountName)
      .put("address", "Main St")
      .put("city", "Baton Rouge")
      .put("state", "LA")
      .put("zip", "70113")
      .toString();
  }

  String exampleTalentJson() {
    return new JSONObject()
      .put("name", accountName)
      .put("address", "Main St")
      .put("city", "Baton Rouge")
      .put("state", "LA")
      .put("zip", "70113")
      .toString();
  }

  void deleteAllAccounts() {
    given().when().delete("/accounts").then().statusCode(200);
  }

  void deleteAllTalent() {
    given().when().delete("/talent").then().statusCode(200);
  }

  String addResource(String uri, String json) {
    return given()
      .contentType(ContentType.JSON)
      .body(json)
      .when()
      .post(uri)
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();
  }

  ValidatableResponse getResource(String uri) {
    return given().when().get(uri).then().statusCode(200);
  }

  ValidatableResponse getResourceWithPathParam(
    String uri,
    String paramName,
    String param,
    Integer statusCode
  ) {
    return given()
      .pathParam(paramName, param)
      .when()
      .get(uri)
      .then()
      .statusCode(statusCode);
  }

  @Test
  public void testAccountsEndpoint() {
    given().when().get("/accounts").then().statusCode(200);
  }

  @Test
  public void testAccountAddAndDeleteAll() {
    deleteAllAccounts();

    String addedAccountURI = addResource("/accounts", exampleAccountJson());

    getResource(addedAccountURI).body("name", equalTo(accountName));

    deleteAllAccounts();
  }

  @Test
  public void getAccount() {
    deleteAllAccounts();

    addResource("/accounts", exampleAccountJson());

    String id = getResource("/accounts").extract().path("[0].id");

    getResourceWithPathParam("/accounts/{id}", "id", id, 200);

    deleteAllAccounts();
  }

  @Test
  public void failOnBadAccountId() {
    deleteAllAccounts();

    getResourceWithPathParam(
      "/accounts/{id}",
      "id",
      "60d1434f7fe4d40a3c74d8c7",
      404
    );
  }

  @Test
  public void testDeleteAccount() {
    deleteAllAccounts();

    addResource("/accounts", exampleAccountJson());

    String id = getResource("/accounts").extract().path("[0].id");

    getResourceWithPathParam("/accounts/{id}", "id", id, 200);

    deleteAllAccounts();
  }

  @Test
  public void testUpdateAccount() {
    deleteAllAccounts();

    String addedAccountURI = addResource("/accounts", exampleAccountJson());

    getResource(addedAccountURI);

    String r = getResource(addedAccountURI).extract().body().asString();

    JSONObject j = new JSONObject(r);
    j.put("name", newName);
    String a1 = j.toString();

    given()
      .contentType(ContentType.JSON)
      .body(a1)
      .when()
      .put(addedAccountURI)
      .then()
      .statusCode(200);

    getResource(addedAccountURI).body("name", equalTo(newName));

    deleteAllAccounts();
  }

  @Test
  public void testAssignManagers() {
    deleteAllAccounts();
    given().when().delete("/talent").then().statusCode(200);

    String addedAccountURI = addResource("/accounts", exampleAccountJson());

    String addedTalentURI = addResource("/talent", exampleTalentJson());

    // String a1 = getResource(addedAccountURI).extract().body().asString();
    String t1 = getResource(addedTalentURI).extract().body().asString();

    // JSONObject aj = new JSONObject(a1);
    JSONObject tj = new JSONObject(t1);

    given()
      .contentType(ContentType.JSON)
      .when()
      .post(addedAccountURI + "/squadManager/" + tj.get("id"))
      .then()
      .statusCode(200);

    getResource(addedAccountURI).body("squadManagerId", equalTo(tj.get("id")));

    given()
      .contentType(ContentType.JSON)
      .when()
      .post(addedAccountURI + "/designManager/" + tj.get("id"))
      .then()
      .statusCode(200);

    getResource(addedAccountURI).body("designManagerId", equalTo(tj.get("id")));

    given()
      .contentType(ContentType.JSON)
      .when()
      .post(addedAccountURI + "/btcManager/" + tj.get("id"))
      .then()
      .statusCode(200);

    getResource(addedAccountURI).body("btcManagerId", equalTo(tj.get("id")));

    // getResource(addedTalentURI).body("accountIds[0]", equalTo(aj.get("id")));

    deleteAllAccounts();
    deleteAllTalent();
  }
}
