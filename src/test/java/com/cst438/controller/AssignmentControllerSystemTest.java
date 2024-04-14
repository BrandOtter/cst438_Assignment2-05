package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AssignmentControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            "C:/chromedriver-win32/chromedriver.exe";

    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void testAddAssignment() throws InterruptedException {
        // Ensure user is of type INSTRUCTOR before running

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Input year and semester data
        WebElement yearInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("year")));
        WebElement semesterInput = driver.findElement(By.id("semester"));

        yearInput.sendKeys("2024");
        semesterInput.sendKeys("Spring");

        // Click on "Show Sections" link
        WebElement showSectionsLink = driver.findElement(By.linkText("Show Sections"));
        showSectionsLink.click();

        // Assuming you land on the InstructorSectionView, wait for a section link to be clickable and click it
        // Adjust this locator to find a specific section if necessary
        WebElement viewAssignmentsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("View Assignments")));
        viewAssignmentsLink.click();

        // Wait for the assignment form's input elements to be available and interact with them
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Title']")));
        WebElement dueDateInput = driver.findElement(By.cssSelector("input[type='date']"));

        // Input sample data
        titleInput.sendKeys("Test Assignment");
        dueDateInput.sendKeys("09092023"); // Adjust format as needed

        // Submit the form
        WebElement addButton = driver.findElement(By.cssSelector("button[type='submit']"));
        addButton.click();

    }

}
