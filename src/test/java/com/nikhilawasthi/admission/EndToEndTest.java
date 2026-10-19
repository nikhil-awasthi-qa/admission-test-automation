package com.nikhilawasthi.admission;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The key test of this project: one application is followed through all three layers.
 * UI (what the student sees) -> API (what the system returns) -> Database (what is stored).
 */
class EndToEndTest extends BaseUiTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    @DisplayName("TC-E2E-01 Student applies in the UI, office approves it, UI, API and database all agree")
    void applicationIsConsistentAcrossUiApiAndDatabase() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // 1. UI: the student submits the form
        String email = TestData.uniqueEmail();
        ApplyPage form = new ApplyPage(driver, baseUrl()).open();
        form.fill("Meera Nair", email, "9988776655", "B.Tech", "88.4");
        form.submit();

        ConfirmationPage confirmation = new ConfirmationPage(driver);
        assertEquals("Application received", confirmation.title());
        long id = confirmation.applicantId();

        // 2. API: the same record is returned with the same data
        given().when().get("/api/applicants/" + id)
                .then().statusCode(200)
                .body("fullName", org.hamcrest.Matchers.equalTo("Meera Nair"))
                .body("email", org.hamcrest.Matchers.equalTo(email))
                .body("course", org.hamcrest.Matchers.equalTo("B.Tech"))
                .body("status", org.hamcrest.Matchers.equalTo("PENDING"));

        // 3. Database: the row exists with the same data
        assertEquals("PENDING",
                jdbc.queryForObject("SELECT status FROM applicants WHERE id = ?", String.class, id));
        assertEquals(email,
                jdbc.queryForObject("SELECT email FROM applicants WHERE id = ?", String.class, id));

        // 4. UI: the admission office approves the application
        ApplicantsPage office = new ApplicantsPage(driver, baseUrl()).open();
        assertEquals("PENDING", office.statusOf(id));
        office.approve(id);
        assertEquals("APPROVED", office.statusOf(id));

        // 5. API and database both show the new status
        given().when().get("/api/applicants/" + id)
                .then().statusCode(200)
                .body("status", org.hamcrest.Matchers.equalTo("APPROVED"));
        assertEquals("APPROVED",
                jdbc.queryForObject("SELECT status FROM applicants WHERE id = ?", String.class, id));
    }

    @Test
    @DisplayName("TC-E2E-02 Rejecting an application in the UI is stored in the database")
    void rejectionIsStored() {
        String email = TestData.uniqueEmail();
        ApplyPage form = new ApplyPage(driver, baseUrl()).open();
        form.fill("Karan Shah", email, "9000011111", "BCA", "65");
        form.submit();
        long id = new ConfirmationPage(driver).applicantId();

        ApplicantsPage office = new ApplicantsPage(driver, baseUrl()).open();
        office.reject(id);
        assertEquals("REJECTED", office.statusOf(id));

        assertEquals("REJECTED",
                jdbc.queryForObject("SELECT status FROM applicants WHERE id = ?", String.class, id));
    }
}
