package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GradeAssignmentSystemTest {


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
    public void testGradeAssignment() throws Exception {

        // Ensure user is of type INSTRUCTOR before running

        // Define constants for sleep duration and the base URL of your application
        final long SLEEP_DURATION = 2000; // Adjust as needed

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Input year and semester data
        WebElement yearInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("year")));
        WebElement semesterInput = driver.findElement(By.id("semester"));

        yearInput.sendKeys("2024");
        semesterInput.sendKeys("Spring");

        // Click on "Show Sections" link
        WebElement showSectionsLink = driver.findElement(By.linkText("Show Sections"));
        showSectionsLink.click();

        // Navigate to the AssignmentsView by clicking the "View Assignments" link for a specific section
        WebElement viewAssignmentsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'View Assignments')]")));
        viewAssignmentsLink.click();

        // Assert that we are on the right page, looking for a header or title that indicates it
        WebElement assignmentsPageHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Assignments')]")));
        assertTrue(assignmentsPageHeader.isDisplayed(), "We are not on the Assignments page.");

        // Click on the 'Grade' button for assignment id 1
        WebElement gradeAssignmentLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//tr[td/text()='1']/td/button[contains(text(),'Grade')]")));
        gradeAssignmentLink.click();

        // Assert that we are on the grading page for the correct assignment
        WebElement gradingPageHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Grades for Assignment')]")));
        assertTrue(gradingPageHeader.isDisplayed(), "We are not on the correct grading page.");

        // Check the default value of the grade and store it
        WebElement defaultGradeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td/text()='1']/td/input[@name='score']")));
        String defaultValue = defaultGradeInput.getAttribute("value");
        assertEquals("95", defaultValue, "The default value of the grade is not 95.");

        // Clear the default score and enter a new score of 90
        defaultGradeInput.clear();
        defaultGradeInput.sendKeys("90");
        // Thread.sleep can be used for debugging but should not be used in actual test automation

        // Submit the scores
        WebElement submitScoresButton = driver.findElement(By.xpath("//button[@type='submit' and contains(text(), 'Update Grades')]"));
        submitScoresButton.click();

        // Switch to the alert box
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept(); // Click the "OK" button on the alert box

        // Wait for the alert to be dismissed and for the page to become interactive again
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//button[@type='submit' and contains(text(), 'Update Grades')]")));

        // Wait for the "Grade" button to become clickable again for assignment id 1
        gradeAssignmentLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//tr[td/text()='1']/td/button[contains(text(),'Grade')]")));
        gradeAssignmentLink.click(); // Click the 'Grade' button for assignment id 1 again

        // Wait for the grading page for the correct assignment to appear again
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Grades for Assignment')]")));

        // Now, locate the input field for the grade that was updated to confirm it has the new value of "90"
        WebElement updatedGradeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td/text()='1']/td/input[@name='score']")));
        String updatedValue = updatedGradeInput.getAttribute("value");
        assertEquals("90", updatedValue, "The grade value was not updated successfully.");

        // Cleanup. Follow a similar course of action using the UI to reset the grade back to 95
        // After verifying that the score has been updated to 90, proceed to reset it back to 95
        WebElement scoreInputToReset = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tr[td/text()='1']/td/input[@name='score']")));
        scoreInputToReset.clear();
        scoreInputToReset.sendKeys("95");

        // Submit the scores again to reset them
        WebElement submitScoresButtonToReset = driver.findElement(By.xpath("//button[@type='submit' and contains(text(), 'Update Grades')]"));
        submitScoresButtonToReset.click();

        // Accept the alert that confirms the scores have been updated
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        Thread.sleep(SLEEP_DURATION);
    }
}
