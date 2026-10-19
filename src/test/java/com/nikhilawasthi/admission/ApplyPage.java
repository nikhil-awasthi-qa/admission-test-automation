package com.nikhilawasthi.admission;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the admission form. Locators live here, tests only describe behaviour. */
class ApplyPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    ApplyPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    ApplyPage open() {
        driver.get(baseUrl + "/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fullName")));
        return this;
    }

    ApplyPage fill(String fullName, String email, String phone, String course, String percentage) {
        type("fullName", fullName);
        type("email", email);
        type("phone", phone);
        if (course != null && !course.isEmpty()) {
            new Select(driver.findElement(By.id("course"))).selectByVisibleText(course);
        }
        type("percentage", percentage);
        return this;
    }

    void submit() {
        driver.findElement(By.id("submitBtn")).click();
    }

    /** Waits until the error message for the given field is visible and returns its text. */
    String waitForError(String field) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(field + "-error"))).getText();
    }

    String errorFor(String field) {
        List<WebElement> found = driver.findElements(By.id(field + "-error"));
        return found.isEmpty() ? "" : found.get(0).getText();
    }

    private void type(String id, String value) {
        WebElement element = driver.findElement(By.id(id));
        element.clear();
        if (value != null && !value.isEmpty()) {
            element.sendKeys(value);
        }
    }
}
