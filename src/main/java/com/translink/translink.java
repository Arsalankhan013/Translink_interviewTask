package com.translink;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class translink {

    WebDriver driver;


    @BeforeTest
    public void LaunchBrowser(){
        WebDriverManager.chromedriver().setup();

        driver=new ChromeDriver();
        driver.get("https://www.translink.ca/");
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test(testName = "Search, Select, and View Schedule for Route #99",priority = 2)
    public void testSearchSelectViewRoute99() {
        Actions action = new Actions(driver);
        WebElement schedule_maps = driver.findElement(By.linkText("Schedules and Maps"));
        action.moveToElement(schedule_maps).perform();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.findElement(By.linkText("Bus")).click();
        driver.findElement(By.id("find-schedule-searchbox")).sendKeys("99");
        driver.findElement(By.xpath("//button[@form='searchAPI']")).click();
        List<WebElement> search_result = driver.findElements(By.xpath("//output[@class='searchResultsList']//descendant::strong"));

        WebDriverWait wait = new WebDriverWait(driver, 10);
        for (WebElement element : search_result) {
            if (element.getText().equals("#99 - UBC B-Line")) {
                wait.until(ExpectedConditions.elementToBeClickable(element));
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", element);
                break;
            }
        }
    }
        @Test(testName = "Update Schedule Filters and Select Stops",dependsOnMethods = "testSearchSelectViewRoute99",priority = 3)
        public void UpdateScheduleFiltersAndSelectStops() {


            WebElement dateandtime1 = driver.findElement(By.xpath("//input[@name='startDate']"));
            dateandtime1.clear();
            dateandtime1.sendKeys(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM:dd")));
            WebElement start_time = driver.findElement(By.xpath("//input[@name='startTime']"));
            start_time.clear();
            start_time.sendKeys("07:00AM");
            WebElement end_time = driver.findElement(By.xpath("//input[@name='endTime']"));
            end_time.clear();
            end_time.sendKeys("08:30AM");
            driver.findElement(By.xpath("//button[@form='SchedulesTimeFilter']")).click();

            ArrayList<String> StoptoSelect = new ArrayList<>();
            StoptoSelect.add("Commercial-Broadway Station @ Bay 5");
            StoptoSelect.add("E Broadway @ Clark Dr");
            StoptoSelect.add("E Broadway @ Fraser St");

            driver.findElement(By.xpath("//button[@aria-controls='StopsPicker-listbox']")).click();

            List<WebElement> stop_list = driver.findElements(By.xpath("//ul[@id='StopsPicker-listbox']//li/label/span"));
            for (int i = 0; i < stop_list.size(); i++) {
                String element = stop_list.get(i).getText();
                if (StoptoSelect.contains(element)) {
                    stop_list.get(i).click();
                }
            }
            driver.findElement(By.xpath("(//button[contains(text(), 'Selected Stops Only')])[1]")).click();

        }

        @Test (dependsOnMethods ={"testSearchSelectViewRoute99","UpdateScheduleFiltersAndSelectStops"} )
        public void addtofavouritesandvalidate() {
            WebElement addF = driver.findElement(By.xpath("//div[@class='flexContainer flexColumn useContentSpacing useFontColorInfinitely']//button[@type='button']"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", addF);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            WebElement add_favName = driver.findElement(By.id("newfavourite"));
            add_favName.clear();
            add_favName.sendKeys("99 UBC B-Line – Morning Schedule");

            driver.findElement(By.xpath("//button[normalize-space()='Add to favourites']")).click();
            driver.findElement(By.xpath("//a[normalize-space()='Manage my favourites']")).click();

            String Actual=driver.findElement(By.xpath("//a[contains(text(),'99 UBC B-Line – Morning Schedule')]")).getText();
            String expected="99 UBC B-Line – Morning Schedule";
            Assert.assertEquals(Actual,expected);
        }
        @AfterTest
        public void tearDown() {
            if (driver != null) {
                driver.quit();
            }



    }
}
