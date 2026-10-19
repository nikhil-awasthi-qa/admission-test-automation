package com.nikhilawasthi.admission;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the page shown after a successful submission. */
class ConfirmationPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    ConfirmationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    String title() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirmationTitle"))).getText();
    }

    long applicantId() {
        return Long.parseLong(driver.findElement(By.id("applicantId")).getText().trim());
    }

    String status() {
        return driver.findElement(By.id("confirmStatus")).getText();
    }
}
