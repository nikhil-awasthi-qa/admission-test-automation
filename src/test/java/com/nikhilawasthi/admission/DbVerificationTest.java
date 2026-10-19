package com.nikhilawasthi.admission;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Database checks: what the application stores must match what the user and the API see. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DbVerificationTest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void configure() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    private int count() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM applicants", Integer.class);
        return total == null ? 0 : total;
    }

    private long createViaApi(String email) {
        return given().contentType(ContentType.JSON).body(TestData.validApplicant(email))
                .when().post("/api/applicants")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    @DisplayName("TC-DB-01 A record created through the API is stored with status PENDING")
    void apiRecordIsStoredAsPending() {
        String email = TestData.uniqueEmail();
        createViaApi(email);

        String status = jdbc.queryForObject("SELECT status FROM applicants WHERE email = ?", String.class, email);
        assertEquals("PENDING", status);
    }

    @Test
    @DisplayName("TC-DB-02 Every field sent to the API is stored correctly")
    void allFieldsAreStored() {
        String email = TestData.uniqueEmail();
        createViaApi(email);

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT full_name, phone, course, percentage FROM applicants WHERE email = ?", email);
        assertEquals("Ravi Kumar", row.get("FULL_NAME"));
        assertEquals("9123456780", row.get("PHONE"));
        assertEquals("B.Tech", row.get("COURSE"));
        assertEquals(82.5, ((Number) row.get("PERCENTAGE")).doubleValue());
    }

    @Test
    @DisplayName("TC-DB-03 Approving through the API updates the row in the database")
    void approvalIsPersisted() {
        String email = TestData.uniqueEmail();
        long id = createViaApi(email);

        given().queryParam("value", "APPROVED")
                .when().put("/api/applicants/" + id + "/status")
                .then().statusCode(200);

        String status = jdbc.queryForObject("SELECT status FROM applicants WHERE id = ?", String.class, id);
        assertEquals("APPROVED", status);
    }

    @Test
    @DisplayName("TC-DB-04 A request that fails validation does not create a row")
    void invalidRequestStoresNothing() {
        int before = count();
        Map<String, Object> body = TestData.validApplicant(TestData.uniqueEmail());
        body.put("phone", "123");

        given().contentType(ContentType.JSON).body(body)
                .when().post("/api/applicants")
                .then().statusCode(400);

        assertEquals(before, count());
    }

    @Test
    @DisplayName("TC-DB-05 The database itself rejects a duplicate email")
    void databaseEnforcesUniqueEmail() {
        String email = TestData.uniqueEmail();
        String sql = "INSERT INTO applicants (full_name, email, phone, course, percentage, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, "Direct Insert", email, "9000000000", "BCA", 70.0, "PENDING",
                Timestamp.valueOf(LocalDateTime.now()));

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(sql, "Direct Insert Two", email,
                "9000000001", "BCA", 71.0, "PENDING", Timestamp.valueOf(LocalDateTime.now())));
    }

    @Test
    @DisplayName("TC-DB-06 Row count grows by exactly one for one successful application")
    void rowCountGrowsByOne() {
        int before = count();
        createViaApi(TestData.uniqueEmail());
        assertEquals(before + 1, count());
    }
}
