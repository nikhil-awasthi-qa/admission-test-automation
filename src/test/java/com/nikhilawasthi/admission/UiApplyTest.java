package com.nikhilawasthi.admission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** UI tests for the admission form (Selenium + Page Object Model). */
class UiApplyTest extends BaseUiTest {

    @Test
    @DisplayName("TC-UI-01 Valid application shows the confirmation page with status PENDING")
    void validApplicationShowsConfirmation() {
        ApplyPage form = new ApplyPage(driver, baseUrl()).open();
        form.fill("Asha Verma", TestData.uniqueEmail(), "9876543210", "MCA", "78.5");
        form.submit();

        ConfirmationPage confirmation = new ConfirmationPage(driver);
        assertEquals("Application received", confirmation.title());
        assertEquals("PENDING", confirmation.status());
    }

    @ParameterizedTest(name = "TC-UI-02.{index} {5}: {6}")
    @CsvSource({
        "'', asha@example.com, 9876543210, MCA, 75, fullName, Full name is required",
        "Asha Verma, '', 9876543210, MCA, 75, email, Email is required",
        "Asha Verma, not-an-email, 9876543210, MCA, 75, email, Email format is invalid",
        "Asha Verma, asha@example.com, 12345, MCA, 75, phone, Phone must be 10 digits",
        "Asha Verma, asha@example.com, 9876543210, '', 75, course, Course is required",
        "Asha Verma, asha@example.com, 9876543210, MCA, 150, percentage, Percentage must be between 0 and 100",
        "Asha Verma, asha@example.com, 9876543210, MCA, -5, percentage, Percentage must be between 0 and 100",
        "Asha Verma, asha@example.com, 9876543210, MCA, '', percentage, Percentage is required"
    })
    @DisplayName("TC-UI-02 Invalid input shows the correct error message")
    void invalidInputShowsError(String fullName, String email, String phone, String course,
                                String percentage, String field, String expectedMessage) {
        ApplyPage form = new ApplyPage(driver, baseUrl()).open();
        form.fill(fullName, email, phone, course, percentage);
        form.submit();

        String actual = form.waitForError(field);
        assertTrue(actual.contains(expectedMessage),
                "Expected '" + expectedMessage + "' but the page showed '" + actual + "'");
    }

    @Test
    @DisplayName("TC-UI-03 Applying twice with the same email is rejected")
    void duplicateEmailIsRejected() {
        String email = TestData.uniqueEmail();

        ApplyPage first = new ApplyPage(driver, baseUrl()).open();
        first.fill("Asha Verma", email, "9876543210", "MCA", "78");
        first.submit();
        assertEquals("Application received", new ConfirmationPage(driver).title());

        ApplyPage second = new ApplyPage(driver, baseUrl()).open();
        second.fill("Asha Verma", email, "9876543210", "MCA", "78");
        second.submit();
        assertTrue(second.waitForError("email").contains("Email already registered"));
    }
}
