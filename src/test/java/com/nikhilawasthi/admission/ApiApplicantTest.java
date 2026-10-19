package com.nikhilawasthi.admission;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** API tests for /api/applicants (Rest Assured). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiApplicantTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void configure() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    private long createApplicant(String email) {
        return given().contentType(ContentType.JSON).body(TestData.validApplicant(email))
                .when().post("/api/applicants")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @DisplayName("TC-API-01 POST with valid data returns 201 and status PENDING")
    void createReturns201AndPending() {
        given().contentType(ContentType.JSON).body(TestData.validApplicant(TestData.uniqueEmail()))
                .when().post("/api/applicants")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("PENDING"))
                .body("fullName", equalTo("Ravi Kumar"));
    }

    @Test
    @DisplayName("TC-API-02 POST with a blank name returns 400 and a clear message")
    void blankNameReturns400() {
        Map<String, Object> body = TestData.validApplicant(TestData.uniqueEmail());
        body.put("fullName", "");
        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/applicants")
                .then()
                .statusCode(400)
                .body("errors.fullName", equalTo("Full name is required"));
    }

    @Test
    @DisplayName("TC-API-03 POST with an invalid email returns 400")
    void invalidEmailReturns400() {
        Map<String, Object> body = TestData.validApplicant("not-an-email");
        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/applicants")
                .then()
                .statusCode(400)
                .body("errors.email", equalTo("Email format is invalid"));
    }

    @Test
    @DisplayName("TC-API-04 POST with a percentage above 100 returns 400")
    void percentageAboveHundredReturns400() {
        Map<String, Object> body = TestData.validApplicant(TestData.uniqueEmail());
        body.put("percentage", 101);
        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/applicants")
                .then()
                .statusCode(400)
                .body("errors.percentage", equalTo("Percentage must be between 0 and 100"));
    }

    @Test
    @DisplayName("TC-API-05 POST with an existing email returns 409")
    void duplicateEmailReturns409() {
        String email = TestData.uniqueEmail();
        createApplicant(email);
        given().contentType(ContentType.JSON).body(TestData.validApplicant(email))
                .when().post("/api/applicants")
                .then()
                .statusCode(409)
                .body("error", equalTo("Email already registered"));
    }

    @Test
    @DisplayName("TC-API-06 GET by id returns the applicant")
    void getByIdReturnsApplicant() {
        String email = TestData.uniqueEmail();
        long id = createApplicant(email);
        given().when().get("/api/applicants/" + id)
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("TC-API-07 GET by an unknown id returns 404")
    void unknownIdReturns404() {
        given().when().get("/api/applicants/99999999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Applicant not found"));
    }

    @Test
    @DisplayName("TC-API-08 GET list contains a newly created applicant")
    void listContainsNewApplicant() {
        String email = TestData.uniqueEmail();
        createApplicant(email);
        given().when().get("/api/applicants")
                .then()
                .statusCode(200)
                .body("email", hasItem(email));
    }

    @Test
    @DisplayName("TC-API-09 PUT status APPROVED updates the applicant")
    void approveUpdatesStatus() {
        long id = createApplicant(TestData.uniqueEmail());
        given().queryParam("value", "APPROVED")
                .when().put("/api/applicants/" + id + "/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("APPROVED"));
    }

    @Test
    @DisplayName("TC-API-10 PUT with an invalid status returns 400")
    void invalidStatusReturns400() {
        long id = createApplicant(TestData.uniqueEmail());
        given().queryParam("value", "MAYBE")
                .when().put("/api/applicants/" + id + "/status")
                .then()
                .statusCode(400)
                .body("error", equalTo("Status must be APPROVED or REJECTED"));
    }
}
