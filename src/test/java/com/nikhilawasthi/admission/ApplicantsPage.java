package com.nikhilawasthi.admission;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the admission office page, where staff approve or reject applications. */
class ApplicantsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    ApplicantsPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    ApplicantsPage open() {
        driver.get(baseUrl + "/applicants");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantsTable")));
        return this;
    }

    String statusOf(long id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#row-" + id + " .status"))).getText();
    }

    void approve(long id) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("approve-" + id))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantsTable")));
    }

    void reject(long id) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reject-" + id))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantsTable")));
    }
}
