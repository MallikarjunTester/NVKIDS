package com.protech.selenium.api.base;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.protech.nvkids.pages.bstl.BatchStatusLogsPage;

import com.protech.selenium.api.design.Browser;
import com.protech.selenium.api.design.Element;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.chrome.ChromeDriver;

import com.protech.nvkids.testcases.bstl.BatchErrorsTestcase;
import com.protech.nvkids.testcases.bstl.BatchStatusLogsTestcase;
import utils.Reporter;


public class SeleniumBase extends Reporter implements Browser, Element {

	public Logger log = Logger.getLogger(BatchErrorsTestcase.class.getName());
	public Properties prop = new Properties();
	public static WebDriverWait wait;
	public SoftAssert softAssert;
	public ExtentReports extent;
	ExtentTest logger; 
	public String hubIPAddress;

	@AfterMethod
	public void afterMethod() 
	{
		//close URL
		driver.close();
	}

	@BeforeClass
	public void beforeclass() throws FileNotFoundException, IOException
	{
		prop.load(new FileInputStream("./Resources/Log4j.properties"));
		PropertyConfigurator.configure(prop);
		author = System.getProperty("user.name");

	}


	@BeforeMethod
	public void beforeMethod(Method m) throws IOException // Method m -> Used to access the properties of @Test - annotations
	{   


		Test testClass = m.getAnnotation(Test.class); 
		//testcaseName = m.getName();                    
		testcaseName = testClass.testName(); // Returns the method name - LeftPanel TC name in Report
		testcaseDec= testClass.description();        // Returns description which is exist inside the @Test (attributes)
		String[] group = new String[testClass.groups().length];

		for (int i = 0; i < testClass.groups().length; i++) {
			group[i] = testClass.groups()[i];	            
			category = category +group[i]+", ";
		}


		if(category!="")
			category =category.substring(0, category.length()-2);
		else 
			category="";

		startApp("chrome", "http://192.168.111.156:81/");
		report();

	}

	/* *****INITIAL SETUP************ */

	@Override
	public void startApp(String browser, String url) 
	{
		try 
		{
			if (browser.equalsIgnoreCase("chrome")) 
			{
				System.setProperty("webdriver.chrome.driver", "./drivers/chromedriver.exe");
				driver = new ChromeDriver();
				//WebDriverManager.chromedriver().setup();
				//driver = new ChromeDriver();

				//test
				log.info("Chrome Browser launched");
				// NgWebDriver ngdriver = new NgWebDriver((JavascriptExecutor)
				// driver);
				// NgWebDriver ngWebDriver = new NgWebDriver(driver);
				// ngWebDriver.waitForAngularRequestsToFinish();
				// driver.findElement(ByAngular.(�name�)).sendKeys(�NSE�);
			} 
			else if (browser.equalsIgnoreCase("firefox")) 
			{
				System.setProperty("webdriver.gecko.driver", "./drivers/geckodriver.exe");
				driver = new FirefoxDriver();
				log.info("Firefox Browser launched");
			} 
			else if (browser.equalsIgnoreCase("ie")) 
			{
				System.setProperty("webdriver.ie.driver", "./drivers/IEDriverServer.exe");
				driver = new InternetExplorerDriver();
				log.info("IE Browser launched");
			}
			driver.get(url);
			log.info("URL Passed");
			driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			driver.manage().window().maximize();
			log.info("Window Maximized");
			//reportStep("The Browser Launched successfully", "pass");
		} 
		catch (Exception e) 
		{
			System.err.println("The Browser Could not be Launched. Hence Failed");
			log.error("Unable to launch browser");
			/*reportStep("The Browser Could not be Launched. Hence Failed","fail");
			Assert.fail("The Browser Could not be Launched");*/
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	/* *****ACTIONS************ */

	@Override
	public void click(WebElement ele) 
	{
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			String text = ele.getText();
			ele.click();
			if(ele.getAttribute("class").contentEquals("ui-menuitem-text"))
			{
				reportStep("The Link '" + text + "' is clicked", "pass");
			}
			else if(ele.getAttribute("class").contains("chevron-down"))
			{
				reportStep("The Grid Accordion Icon is clicked", "pass");
			}
			else if(ele.getAttribute("class").contains("chkbox"))
			{
				reportStep("The Checkbox is clicked", "pass");
			}
			else
			{
				reportStep("The element is clicked", "pass");
			}
		} 
		catch (Exception e) 
		{
			int a = e.toString().indexOf("Exception:");
			String str = e.toString().substring(0, a + 10);
			reportStep("The element not clicked because of - " + str, "fail");
			log.error("The element is not accessible");
			Assert.fail("The element is not accessible");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	/* This method is used for clicking dropdown,LOV, radiobutton and checkbox */
	@Override
	public void clickIcon(WebElement ele,WebElement label) 
	{

		String attribute = "";
		String quicknavi = "";
		String labeltext="";
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			Thread.sleep(1000);
			attribute = ele.getAttribute("class");
			quicknavi = ele.getAttribute("title");
			labeltext=label.getText();

			ele.click();
			log.info("The Element '" + labeltext + "' is clicked");
			if(attribute.contains("chkbox")){
				reportStep("The checkbox '" + labeltext + "' is clicked", "pass");
				reportStep("The checkbox is clicked", "info");}
			else if(attribute.contains("Icon") && !attribute.contains("quickNavigate"))
				reportStep("The Icon '" + labeltext.replaceAll("\\d","") + "' is clicked", "pass");

			else if(attribute.contains("quickNavigate"))
				reportStep("The Icon '" + quicknavi + "' is clicked", "pass");
			else if(attribute.contains("grid-filter"))
				reportStep("The Grid Options Icon is clicked", "pass");

			else
				reportStep("The Element '" + labeltext + "' is clicked", "pass");
		} 
		catch (Exception e)
		{
			int a = e.toString().indexOf("Exception:");
			String str = e.toString().substring(0, a + 10);
			reportStep("The Icon could not be clicked - " + str, "fail");
			log.error("The Icon  is not clicked");
			//Assert.fail("The Icon  is not clicked");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	/* This method is used for clicking hyperlinks */
	@Override
	public void clickLink(WebElement ele) 
	{
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			String text = ele.getText();
			ele.click();
			log.info("The Link '" + text + "' is clicked");
			reportStep("The Link '" + text+ "' is clicked", "pass");
		} 
		catch (Exception e) 
		{
			int a = e.toString().indexOf("Exception:");
			String str = e.toString().substring(0, a + 10);
			reportStep("The Link  could not be clicked - " + str, "fail");
			log.error("The Link  could not be clicked");
			Assert.fail("Link not accessible");
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void clear(WebElement ele) 
	{
		try 
		{   
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			ele.clear();
			log.info("The field is cleared Successfully");
			reportStep("The data in the field cleared Successfully", "pass");
		} 
		catch (ElementNotInteractableException e) 
		{   
			int a = e.toString().indexOf("Exception:");
			String str = e.toString().substring(0, a + 10);
			reportStep("The data in the field is not cleared"+ str, "fail");
			log.error("The field is not Interactable");
			Assert.fail("The field is not Interactable");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void clearAndType(WebElement ele,WebElement label, String data) {
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			ele.clear();
			log.info("The field is cleared Successfully");
			ele.sendKeys(data);
			if(ele.getAttribute("value").equals(""))
			{
				reportStep("The Data '" + data + "' is not entered in the '"+label.getText()+"' field", "fail");
			}
			else
			{
				log.info("The data " +data+ " is entered into the field Successfully");
				reportStep("The Data '" + data + "' entered Successfully in the '"+label.getText()+"' field", "pass");
			}

		} 
		catch (Exception e) 
		{   
			int a = e.toString().indexOf("Exception:");
			String str = e.toString().substring(0, a + 10);
			reportStep("The Element is not Interactable" + str , "fail");
			log.error("The Element is not Interactable");
			Assert.fail("The Element is not Interactable");
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void selectDropDownUsingText(WebElement ele, String value) 
	{
		try 
		{  
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			new Select(ele).selectByVisibleText(value);
			log.info("The dropdownvalue " + value + " is successfully selected");
			reportStep("The dropdownvalue " + value + " is successfully selected", "pass");
		} 
		catch (Exception e) 
		{
			reportStep("The dropdownvalue " + value + " is not selected", "fail");
			log.error("The dropdownvalue " + value + " is not selected");
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}

	}

	@Override
	public void selectDropDownUsingIndex(WebElement ele, int index) 
	{
		new Select(ele).selectByIndex(index);
		try 
		{
			new Select(ele).selectByIndex(index);
			reportStep("The  " + index + 1 + " th dropdownvalue is successfully selected", "pass");
			log.info("The  " + index + 1 + " th dropdownvalue is successfully selected");
		} 
		catch (Exception e) 
		{
			reportStep("The  " + index + 1 + " th dropdownvalue is not selected", "fail");
			log.error("The  " + index + 1 + " th dropdownvalue is not selected");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}

	}

	@Override
	public void selectDropDownUsingValue(WebElement ele, String value) 
	{
		try 
		{
			new Select(ele).selectByValue(value);
			reportStep("The dropdowntext contains value is " + value + "   successfully selected", "pass");
			log.info("The dropdowntext contains value is " + value + "   successfully selected");
		} 
		catch (Exception e) 
		{
			reportStep("The dropdowntext contains value is " + value + "   not selected", "fail");
			log.error("The dropdowntext contains value is " + value + "   not selected");
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void selectDropdownValue(WebElement ele,String value) 
	{
		try 
		{
			wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			ele.click();
			List<WebElement> options = driver.findElements(By.xpath("//li[@role='option']/span"));
			for(WebElement a : options)
			{
				for(int i=0;i<options.size();i++)
				{
					if(options.get(i).getText().equals(value))
					{
						options.get(i).click();
						reportStep("The dropdowntext '" + options.get(i).getText() + "'is successfully selected", "pass");
						break;
					}
					else
					{
						reportStep("The dropdowntext '" + options.get(i).getText() + "'is not selected", "fail");
						Assert.fail();				
					}
				}

			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to access the element " , "fail");
			log.error("Unable to access the element" );
			Assert.fail();	
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}

	}

	@Override
	public void selectRandomDropdownValue(WebElement ele) 
	{
		try 
		{	
			wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			ele.click();
			List<WebElement> options = driver.findElements(By.xpath("//li[@role='option']/span[not(contains(text(),'--Select--'))]"));
			int dropdownsize = options.size();
			int randnMumber = ThreadLocalRandom.current().nextInt(0, dropdownsize);
			options.get(randnMumber).click();
			if(!options.get(randnMumber).getText().equals(""))
			{
				reportStep("The dropdowntext '" + options.get(randnMumber).getText() + "'is successfully selected", "pass");
			}
			else
			{
				reportStep("The dropdowntext '" + options.get(randnMumber).getText() + "'is not selected", "fail");
				Assert.fail();				
			}

		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to access the element " , "fail");
			log.error("Unable to access the element" );
			Assert.fail();	
		} 
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}

	}

	@Override
	public void switchToAlert() 
	{
		try 
		{
			Alert alert = driver.switchTo().alert();
			reportStep("Successfully switched to Alert window", "pass");
			log.info("Successfully switched to Alert window");
		}
		catch (Exception e) 
		{
			reportStep("failed to switch Alert window", "fail");
			log.error("failed to switch Alert window");
		}
	}

	@Override
	public void acceptAlert() 
	{
		String text = "";
		try 
		{
			Alert alert = driver.switchTo().alert();
			text = alert.getText();
			log.info("Alert text retrieved");
			alert.accept();
			log.info("The alert " + text + " is accepted");
			reportStep("Alert successfully accepted", "pass");
		} 
		catch (NoAlertPresentException e) 
		{
			System.out.println("There is no alert present");
			log.error("There is no alert present");
		} 
		catch (WebDriverException e) 
		{
			System.out.println("WebDriverException : " + e.getMessage());
			reportStep("Failed to Accept Alert", "fail");
			log.error("Failed to Accept Alert");
		}

	}

	@Override
	public void dismissAlert() 
	{
		String text = "";
		try 
		{
			Alert alert = driver.switchTo().alert();
			text = alert.getText();
			log.info("Alert text retrieved");
			alert.dismiss();
			log.info("The alert " + text + " is dismissed");
			reportStep("Alert successfully dismissed", "pass");
		} 
		catch (NoAlertPresentException e) 
		{
			System.out.println("There is no alert present");
			log.error("There is no alert present");
		} 
		catch (WebDriverException e) 
		{
			System.out.println("WebDriverException : " + e.getMessage());
			reportStep("Failed to dismiss Alert", "fail");
			log.error("Failed to Accept Alert");
		}

	}

	@Override
	public void typeAlert(String data) 
	{
		try 
		{
			driver.switchTo().alert().sendKeys(data);
			log.info("The data" + data + "entered on Alert window");
			reportStep("The data" + data + "entered on Alert window", "pass");
		} 
		catch (Exception e)
		{
			reportStep("The data" + data + " is not entered on Alert window", "fail");
			log.error("The data" + data + " is not entered on Alert window");
		}

	}

	@Override
	public void switchToWindow(int windowNumber) 
	{
		try 
		{
			Set<String> allWindows = driver.getWindowHandles();
			List<String> allhandles = new ArrayList<>(allWindows);
			log.info("Retrieved all active windows");
			String exWindow = allhandles.get(windowNumber);
			driver.switchTo().window(exWindow);
			log.info("Successfully switched to the window index " + windowNumber);
			System.out.println("The Window With index: " + windowNumber + " switched successfully");
			reportStep("Successfully switched to the window index" + windowNumber, "pass");
		} 
		catch (NoSuchWindowException e) 
		{
			System.err.println("The Window With index: " + windowNumber + " not found");
			reportStep("Switching to the window index" + windowNumber+ " unsuccessful", "fail");
			log.error("Switching to the window index" + windowNumber+ " unsuccessful");
		}
	}

	@Override
	public void switchTo_AllWindow(String title) 
	{
		try 
		{
			Set<String> allWindows = driver.getWindowHandles();
			log.info("Retrieved all active windows");
			for (String eachWindow : allWindows) 
			{
				driver.switchTo().window(eachWindow);
				if (driver.getTitle().equals(title)) 
				{
					log.info("The Window With Title: " + title + "is switched");
					break;
				}
			}
			System.out.println("The Window With Title: " + title + "is switched");
		}
		catch (NoSuchWindowException e) 
		{
			System.err.println("The Window With Title: " + title + " not found");
			log.error("The Window With Title: " + title + "not found");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void switchToFrame(int index) 
	{
		try 
		{
			driver.switchTo().frame(index);
			log.info("Successfully switched to the frame No. " +index);
			reportStep("Successfully switched to the frame", "pass");
		}
		catch (Exception e) 
		{
			reportStep("Failed to switch to the frame", "fail");
			log.error("Failed to switch to the frame No. " +index);
		}

	}

	@Override
	public void switchToFrame(WebElement ele) 
	{
		try 
		{
			driver.switchTo().frame(ele);
			log.info("Successfully switched to the frame " +ele);
			reportStep("successfully switched to the frame", "pass");
		}
		catch (Exception e) 
		{
			reportStep("failed to switch to the frame", "fail");
			log.error("Failed to switch to the frame " +ele);
		}
	}

	@Override
	public void switchToFrame(String idOrName) 
	{
		try 
		{
			driver.switchTo().frame(idOrName);
			log.info("Successfully switched to the frame " +idOrName);
			reportStep("successfully switched to the frame", "pass");

		}
		catch (Exception e) 
		{
			reportStep("failed to switch to the frame", "fail");
			log.error("Failed to switch to the frame " +idOrName);
		}

	}

	@Override
	public void defaultContent() 
	{
		try 
		{
			driver.switchTo().defaultContent();
			log.info("Successfully switched to the default frame");
			reportStep("successfully switched to the default frame", "pass");
		}
		catch (Exception e) 
		{
			reportStep("Failed to switch to the default frame", "fail");
			log.error("Failed to switch to the default frame");
		}

	}

	@Override
	public long takeSnap() 
	{
		long number = (long) Math.floor(Math.random() * 900000000L) + 10000000L;
		try 
		{
			FileUtils.copyFile(driver.getScreenshotAs(OutputType.FILE),
					new File("./reports/images/" + number + ".jpg"));
		} 
		catch (WebDriverException e) 
		{
			System.out.println("The browser has been closed");
		} 
		catch (IOException e)
		{
			System.out.println("The snapshot could not be taken");
		}
		return number;
	}

	@Override
	public void setCurrentDate(WebElement ele) 
	{
		try 
		{
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date date = new Date();
			String systemdate = dateFormat.format(date);
			systemdate =systemdate.replace("/", "");
			clear(ele);
			ele.sendKeys(systemdate);
			log.info("Current date " +systemdate+ " successfully entered");
			reportStep("Current date successfully entered in '"+ele.getAttribute("id")+"'field", "pass");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("failed to enter current date in '"+ele.getAttribute("id")+"'field", "fail");
			log.error("Failed to enter current date");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public String setFutureDate(WebElement ele) 
	{
		String futuredate="";
		try 
		{
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date currentDate = new Date();
			System.out.println(dateFormat.format(currentDate));
			// convert date to calendar
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, 1);
			futuredate = dateFormat.format(c.getTime());
			futuredate=futuredate.replace("/", "");
			clear(ele);
			ele.sendKeys(futuredate);
			log.info("Future date " +futuredate+ " successfully entered");
			reportStep("Future date successfully entered in '"+ele.getAttribute("id")+"'field", "pass");

		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Failed to enter future date in '"+ele.getAttribute("id")+"'field", "fail");
			Assert.fail();
			log.error("Failed to enter future date");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
		return futuredate;
	}

	@Override
	public void setPastDate(WebElement ele) 
	{
		try 
		{
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date currentDate = new Date();
			System.out.println(dateFormat.format(currentDate));
			// convert date to calendar
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -1);
			String pastdate = dateFormat.format(c.getTime());
			pastdate=pastdate.replace("/", "");
			System.out.println("durai:"+pastdate);
			clear(ele);
			ele.sendKeys(pastdate);
			log.info("Past date " +pastdate+ " successfully entered");
			System.out.println("Past date ---> " + pastdate.replace("/", ""));
			reportStep("past date successfully entered in '"+ele.getAttribute("id")+"'field", "pass");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("failed to enter future date in '"+ele.getAttribute("id")+"'field", "fail");
			log.error("Failed to enter past date");
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void selectDateFromCalendar(WebElement ele,int date) 
	{
		String expectedDate = Integer.toString(date);
		try 
		{
			List<WebElement> columns=ele.findElements(By.tagName("td"));
			for (WebElement cell: columns)
			{
				if (cell.getText().equals(expectedDate))
				{
					cell.findElement(By.linkText(expectedDate)).click();
					log.info("Date selected from calendar");
					reportStep("Successfully selected a date from calendar", "pass");
					break;
				}
			}
		}
		catch (Exception e) 
		{
			reportStep("Unable to select a date from calendar", "fail");
			log.error("Unable to select a date from calendar");
		}
	}

	@Override
	public void uploadFile(String path) throws IOException 
	{
		try 
		{
			// precondition :AutoIT executable file must be exist
			Runtime.getRuntime().exec(path);
			log.info("File uploaded successfully from path: "+path);
			reportStep("File uploaded successfully", "pass");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Failed to upload File", "fail");
			log.error("Failed to upload File from path: "+path);
		}
	}

	@Override
	public void pressKey(WebElement ele, String key) throws IOException 
	{
		// pass data as "Keys.TAB"
		try 
		{
			ele.sendKeys(key);
			log.info("Successfully pressed the key  " + key + "from the keyboard");
			reportStep("Successfully pressed the key  " + key + "from the keyboard", "pass");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Failed to press the key  " + key + "from the keyboard", "fail");
			log.info("Failed to press the key  " + key + "from the keyboard");
		}
	}

	@Override
	public void delay(int waittime) throws IOException, InterruptedException 
	{
		Thread.sleep(waittime);
		log.info("Execution halted for " +waittime);
	}

	@Override
	public void closeBrowser() 
	{
		driver.close();
		log.info("Browser closed");
	}

	@Override
	public void deleteAllcookies() 
	{
		driver.manage().deleteAllCookies();
		log.info("Cookies cleared");
	}

	@Override
	public void executeJavaScript(WebElement element) 
	{
		try 
		{
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].click()", element);
			System.out.println("The element" + element + "clicked successfully");
		}
		catch (Exception e) 
		{   
			System.out.println("The JS couldn't click element");
		}
	}

	@Override
	public void forWard() 
	{
		driver.navigate().forward();
		log.info("Page forwarded");
	}

	@Override
	public void reFresh() 
	{
		driver.navigate().refresh();
		log.info("Page refreshed");
	}

	@Override
	public void scrolltoPosition(String value) 
	{
		try 
		{
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript(value);

		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}


	@Override
	public void elementscrollPosition(WebElement element) 
	{
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView()", element);
	}

	@Override
	public void killBrowser(String killpath) throws IOException 
	{
		Runtime.getRuntime().exec(killpath);
		/*
		 * Every java app has this instance of runtime class that allows the
		 * application to interface with the environment in which the
		 * application is running. exec() it executes a string command in a
		 * separate process
		 */
		System.out.println("Browser killed successfully");
		log.info("Browser killed successfully");
	}

	@Override
	public void deselectAlloptions(int waittime, WebElement... elements) 
	{
		try 
		{
			WebElement checkElement = null;
			if (elements.length > 0) 
			{
				for (WebElement currentElement : elements) 
				{
					currentElement = checkElement;
					WebDriverWait wait = new WebDriverWait(driver, waittime);
					wait.until(ExpectedConditions.elementToBeClickable(currentElement));
					WebElement checkbox = currentElement;
					if (checkbox.isSelected()) 
					{
						System.out.println("The Element " + currentElement + "is checked");
					} 
					else
						checkbox.click();
					log.info("All options deselected");
					reportStep("All options deselected", "pass");
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Not able to deselect all options", "fail");
			log.error("Not able to deselect all options");
		}
	}

	@Override
	public void close() 
	{
		driver.close();

	}

	@Override
	public void quit() 
	{
		driver.quit();
	}

	@Override
	public void doRightClick(WebElement ele) 
	{
		try 
		{
			Actions actions = new Actions(driver);
			actions.contextClick(ele).perform();
			log.info("Right Click Performed on " + ele);
			System.out.println("Right Click Performed on " + ele);
			reportStep(ele + " is right clicked", "pass");
		}
		catch (NoSuchElementException e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to right click " + ele + "--> " + error, "fail");
			System.err.println("Unable to do right click on " + ele);
			log.error("Unable to do right click on " + ele);
			throw new RuntimeException();
		}
	}

	@Override
	public void setFocus(WebElement ele) 
	{
		try 
		{
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			/*
			 * wait = new WebDriverWait(driver, 10);
			 * wait.until(ExpectedConditions.elementToBeClickable(ele));
			 */
			if ("input".equals(ele.getTagName())) 
			{
				ele.sendKeys("");
				log.info("The Element " + ele + " focussed");
				reportStep("The Element " + ele + " focussed", "pass");
			}
			else if ("select".equals(ele.getTagName())) 
			{
				ele.sendKeys("");
				log.info("The Element " + ele + " focussed");
				reportStep("The Element " + ele + " focussed", "pass");
			}
			else if ("button".equals(ele.getTagName())) 
			{
				ele.sendKeys("");
				log.info("The Element " + ele + " focussed");
				reportStep("The Element " + ele + " focussed", "pass");
			}
			else if ("a".equals(ele.getTagName())) 
			{
				ele.sendKeys("");
				log.info("The Element " + ele + " focussed");
				reportStep("The Element " + ele + " focussed", "pass");
			}

		} 
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("The Element " + ele + " could not be focussed --> " + error, "fail");
			System.err.println("The Element " + ele + " with tag name " + ele.getTagName() + " could not be focussed");
			log.error("The Element " + ele + " with tag name " + ele.getTagName() + " could not be focussed");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public String decode(String data) 
	{
		byte[] decoded = Base64.decodeBase64(data);
		log.info("String: " +data+ " decoded");
		return new String(decoded);
	}

	@Override
	public void clickOffset(WebElement ele, int xOffset, int yOffset) 
	{
		try 
		{
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			/*
			 * wait = new WebDriverWait(driver, 10);
			 * wait.until(ExpectedConditions.elementToBeClickable(ele));
			 */
			Actions build = new Actions(driver);
			build.moveToElement(ele, xOffset, yOffset).click().build().perform();
			log.info("The Element " + ele + " clicked");
			reportStep("The Element " + ele + " clicked", "pass");
			System.out.println("The Element " + ele + " clicked");
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("The Element " + ele + " could not be clicked --> " + error, "fail");
			System.err.println("The Element " + ele + " could not be clicked");
			log.error("The Element " + ele + " could not be clicked");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void doDoubleClick(WebElement ele) 
	{
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));
			Actions build = new Actions(driver);
			build.doubleClick(ele).build().perform();
			log.info("The Element " + ele + " double-clicked");
			reportStep("The Element " + ele + " double-clicked", "pass");
			System.out.println("The Element " + ele + " double-clicked");
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("The Element " + ele + " could not be clicked --> " + error, "fail");
			System.err.println("The Element " + ele + " could not be clicked");
			log.error("The Element " + ele + " could not be clicked");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	// ******RETURN VALUES USING GETMETHODS************

	@Override
	public void getElementWidth(WebElement ele) 
	{
		try 
		{
			int width = ele.getSize().getWidth();
			log.info("Retrieved Element Width is " + width + " pixels");
			System.out.println("Element Width is " + width + " pixels");
		} 
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to find element --> " + error, "fail");
			System.err.println("Unable to find element");
			log.error("Unable to find element");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void getElementHeight(WebElement ele) 
	{
		try 
		{
			int height = ele.getSize().getHeight();
			log.info("Retrieved Element Height is " + height + " pixels");
			System.out.println("Element Height is " + height + " pixels");
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to find element --> " + error, "fail");
			System.err.println("Unable to find element");
			log.error("Unable to find element");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public String getElementText(WebElement ele) 
	{
		try 
		{
			ele.getText();
			log.info("Successfully got element text from " +ele);
			reportStep("Successfully got element text ", "pass");
		}
		catch (Exception e) 
		{
			reportStep("Failed to get element text " + ele, "pass");
			log.error("Failed to get element text " + ele);
			e.printStackTrace();
		}
		return ele.getText();
	}

	@Override
	public String getBackgroundColor(WebElement ele) 
	{
		try 
		{
			String s = ele.getCssValue("color");
			log.info("Successfully got element backround color from " + ele);
			reportStep("Successfully got element backround color from " + ele, "pass");
			return s;
		}
		catch (Exception e) 
		{
			reportStep("Failed to get element backround color", "pass");
			log.error("Failed to get element backround color from " + ele);
			e.printStackTrace();
		}
		return ele.getCssValue("color");
	}

	@Override
	public void verifyBorderColor(WebElement... elements) 
	{
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfAllElements(elements));
			for (WebElement ele : elements) 
			{
				if(ele.getCssValue("border-color").contains("rgb(175, 175, 175)"))
				{
					reportStep("The element is not highlighted in red", "fail");
					log.info("The element is not highlighted in red");
					Assert.fail();
				}
				else if(ele.getCssValue("border-color").contains("rgb(236, 77, 77)"))
				{
					reportStep("The element is highlighted in red", "pass");
					log.info("The element is highlighted in red");
				}
				else
				{
					System.out.println("Actual border color is :" +ele.getCssValue("border-color"));
				}

			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Failed to get element border color", "fail");
			log.error("Failed to get element border color");
			Assert.fail();
		}
	}

	@Override
	public String getTypedText(WebElement ele) 
	{
		try 
		{
			String s = ele.getAttribute("value");
			log.info("Successfully got entered text on field " + ele);
			reportStep("Successfully got entered text on field " + ele, "pass");
			return s;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Failed to get entered text " + ele, "fail");
			log.error("Failed to get entered text " + ele);
		}
		return ele.getAttribute("value");
	}

	@Override
	public String getAlertText() 
	{
		Alert alert = driver.switchTo().alert();
		try 
		{
			String s = alert.getText();
			log.info("Successfully retrieved Alert Text");
			reportStep("Successfully retrieved Alert Text", "pass");
			return s;
		}
		catch (NoAlertPresentException e) 
		{
			System.out.println("There is no alert present.");
			reportStep("Failed to retrieve alert text", "fail");
			log.error("Failed to retrieve alert text");
		} 
		catch (WebDriverException e) 
		{
			reportStep("Failed to retrieve alert text", "fail");
			log.error("Failed to retrieve alert text");
			System.out.println("WebDriverException : " + e.getMessage());
		}
		return null;
	}

	@Override
	public WebElement locateElement(String value) 
	{
		try 
		{
			WebElement findElementById = driver.findElementById(value);
			log.info("The element with ID = " + value + "is successfully located");
			reportStep("The element " + value + "is successfully located", "pass");
			return findElementById;
		}
		catch (Exception e) 
		{
			reportStep("Unable to locate the element with ID = " + value, "fail");
			log.error("Unable to locate the element with ID = " + value);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<WebElement> locateElements(String type, String value) 
	{
		try 
		{
			switch (type.toLowerCase()) 
			{
			case "id":
				List<WebElement> ele = driver.findElementsById(value);
				log.info("The element with ID = " + value + "is successfully located");
				reportStep("Elements located using " + type + "", "pass");
				return ele;
			case "name":
				List<WebElement> ele1 = driver.findElementsByName(value);
				log.info("The element with Name = " + value + "is successfully located");
				reportStep("Elements located using " + type + "", "pass");
				return ele1;
			case "class":
				List<WebElement> ele2 = driver.findElementsByClassName(value);
				log.info("The element with ClassName = " + value + "is successfully located");
				reportStep("Elements located using " + type + "", "pass");
				return ele2;
			case "link":
				List<WebElement> ele3 = driver.findElementsByLinkText(value);
				log.info("The element with LinkText = " + value + "is successfully located");
				reportStep("Elements located using " + type + "", "pass");
				return ele3;
			case "xpath":
				List<WebElement> ele4 = driver.findElementsByXPath(value);
				log.info("The element with XPath = " + value + "is successfully located");
				reportStep("Elements located using " + type + "", "pass");
				return ele4;
			}
		}
		catch (Exception e) 
		{
			System.err.println("The Element with locator:" + type + " Not Found with value: " + value);
			reportStep("The Element with locator:" + type + " Not Found with value: " + value,"fail");
			log.error("The Element with locator: " + type + " Not Found with value: " + value);
			throw new RuntimeException();
		}
		return null;
	}

	@Override
	public String getAttribute(WebElement ele, String data) throws IOException 
	{
		try 
		{
			String s = ele.getAttribute(data);
			log.info("Succesfully retrieved " +ele+ "'s  attribute value for " + data);
			reportStep("Succesfully retrieved " + ele + "'s attribute value for " + data + "", "pass");
			return s;
		}
		catch (Exception e) 
		{
			reportStep("Failed to retrieve" + ele + "  attribute value for " + data + "", "fail");
			log.error("Failed to retrieve" + ele + "  attribute value for " + data);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ArrayList<String> findAllLinksOnPage() 
	{
		ArrayList<String> data = new ArrayList<String>();
		try 
		{
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			List<WebElement> allLinks = driver.findElements(By.tagName("a"));
			System.out.println("Total No.of Links in the page: " + allLinks.size());
			for (WebElement link : allLinks) 
			{
				System.out.println(link.getText());
				data.add(link.getText());
			}
			reportStep("Links are fetched", "pass");
			System.out.println("Links are fetched");
			return data;
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to get all the links --> " + error, "fail");
			System.err.println("Unable to get all the links");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
		}
	}

	@Override
	public String getURL() 
	{
		try 
		{
			String s = driver.getCurrentUrl();
			log.info("Successfully retrieved current URL");
			reportStep("Successfully retrieved current URL", "pass");
			return s;
		} 
		catch (Exception e) 
		{
			reportStep("Retrieving current URL failed ", "fail");
			log.error("Retrieving current URL failed");
			e.printStackTrace();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
		return null;
	}

	// ******VERIFY TEXT************

	@Override
	public boolean verifyElementHasAttribute(WebElement ele, String attribute) 
	{
		Boolean result = false;
		try 
		{
			String value = ele.getAttribute(attribute);
			log.info("Attribute retrieved for "+ele+ " and stored in variable: value");
			if (value != null) 
			{
				result = true;
				log.info("Element has the attribute " + attribute);
				reportStep("Element has the attribute " + attribute, "pass");
				System.out.println("Element has the attribute: " + attribute);
			}
		} 
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to verify attribute --> " + error, "fail");
			log.error("Unable to verify attribute");
			System.err.println("Unable to verify attribute");
			throw new RuntimeException();
		}
		return result;
	}

	@Override
	public void verifyUrl(String url) 
	{
		try 
		{
			assertEquals(driver.getCurrentUrl(), url);
			log.info("URL's matched");
			reportStep("URL matched", "pass");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("URL's not matched", "fail");
			log.error("URL's not matched");
		}
	}

	@Override
	public void verifyTitle(String title) 
	{
		try 
		{
			assertEquals(driver.getTitle(), title);
			log.info("Title matched");
			reportStep("Title matched", "pass");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Title not matched", "fail");
			log.error("Title not matched");
		}
	}

	@Override
	public void verifyExactText(WebElement ele, String expectedText) 
	{
		try 
		{
			wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.visibilityOf(ele));
			if(ele.getText().equals(expectedText))
			{
				reportStep("The expected text contains the actual '" + expectedText +"'", "pass");
				log.info("The expected text contains the actual '" + expectedText +"'");
			}
			else
			{
				reportStep("The expected text does not contains the actual '" + expectedText +"'", "fail");
				log.info("The expected text does not contains the actual '" + expectedText +"'");
				Assert.fail("The expected text does not contains the actual '" + expectedText +"'");
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("Unable to access the element ", "fail");
			log.error("Unable to access the element ");
			Assert.fail("Unable to access the element");
		}
	}

	@Override
	public void verifyPartialText(WebElement ele, String expectedText) 
	{
		try
		{
			wait = new WebDriverWait(driver, 20);
			wait.until(ExpectedConditions.visibilityOf(ele));
			if(ele.getText().contains(expectedText)){
				System.out.println("Actual text is"+ele.getText());
				log.info("The expected text contains the actual " + expectedText);
				reportStep("The expected text contains the actual " + expectedText, "pass");
			}
			else
			{
				reportStep("The expected text doesn't contain the actual " + expectedText, "fail");
				Assert.fail("The expected text doesn't contains the actual " + expectedText);
				log.error("The expected text doesn't contain the actual - " + expectedText);

			}
		}
		catch(Exception e)
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to Access the Element", "fail");
			Assert.fail("The expected text doesn't contain the actual " + expectedText);
			log.error("The expected text doesn't contain the actual - " + expectedText);
		}
	}

	@Override
	public boolean verifyExactAttribute(WebElement ele, String attribute, String value) 
	{
		try 
		{
			if (ele.getAttribute(attribute).equals(value)) 
			{
				log.info("The expected attribute :" + attribute + " value contains the actual " + value);
				System.out.println("The expected attribute :" + attribute + " value contains the actual " + value);
				return true;
			}
			/*else 
			{
				System.out.println("The expected attribute :" + attribute + " value does not contains the actual " + value);

			}*/
		} 
		catch (WebDriverException e) 
		{
			System.out.println("Unknown exception occured while verifying the Attribute Text");
			log.error("Unknown exception occured while verifying the Attribute Text");
			return false;
		}
		return false;
	}

	@Override
	public boolean verifyPartialAttribute(WebElement ele, String attribute, String value) 
	{
		try 
		{
			if (ele.getAttribute(attribute).contains(value)) 
			{
				log.info("The expected attribute :" + attribute + " value contains the actual " + value);
				System.out.println("The expected attribute :" + attribute + " value contains the actual " + value);
				return true;
			}
			/*			else 
			{
				System.out.println("The expected attribute :" + attribute + " value does not contains the actual " + value);

			}*/
		} 
		catch (WebDriverException e) 
		{
			System.out.println("Unknown exception occured while verifying the Attribute Text");
			log.error("Unknown exception occured while verifying the Attribute Text");
			return false;
		}
		return false;
	}

	@Override
	public void verifyAssociatedScreens(String...expected) {
		try 
		{
			ArrayList<String> Headers = new ArrayList<String>();
			ArrayList<String> expec = new ArrayList<String>();
			List<WebElement> GridValues = driver.findElementsByXPath("//li[contains(@class,'associatedMenuItem ')]/a/span[2]");
			for (WebElement col : GridValues){
				Headers.add(col.getText().trim());
				//System.out.println(col.getText());

			}
			for(String exp : expected ){
				expec.add(exp.trim());
				//System.out.println(exp);
			}

			if (expec.equals(Headers)) 
			{
				log.info("Associated Screens are present");
				for(String display : expected){
					System.out.println(display);
					reportStep("Associated Screen "+ display+" is present", "pass");
				}


			}
			else{

				log.info("Associated Screens are not present");
				reportStep("Associated Screens are not present", "fail");
				Assert.fail();
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find Associated Screens --> " + error, "fail");
			log.error("Unable to fid Associated Screens --> " + error);
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}

	}

	// ******VERIFY FIELDS VISIBILTY************

	@Override
	public void verifyDisplayed(WebElement label,WebElement ele) throws Exception 
	{
		try 
		{
			wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOf(ele));
			if (label.isDisplayed()==true && ele.isDisplayed() == true) 
			{
				reportStep("The field '"+ label.getText() + "' is  displayed in the screen", "pass");
			}

			else
			{
				reportStep("The field '"+ label.getText() + "' is  not displayed in the screen", "fail");
				log.error(label.getText() + " is not displayed");
				Assert.fail("Element "+label.getText()+" not displayed");
			}
		} 
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to find Element --> " + error, "fail");
			log.error("Unable to find Element ");
			//Assert.fail("Unable to find Element");
		}
	}

	@Override
	public void verifyFieldIsEmpty(WebElement ele)
	{

		try{
			ele.clear();
			if (ele.getAttribute("value").isEmpty() == true)
			{
				reportStep(ele.getText() +" is Empty","pass");	
			}
			else
			{
				reportStep(ele.getText() +" is Not Empty","fail");	
			}
		}
		catch(Exception e){
			reportStep("Unable to find the field value","fail");	
		}


	}

	@Override
	public void softAssertDisplayed(WebElement ele) 
	{
		softAssert.assertTrue(ele.isDisplayed());
		softAssert.assertAll();
	}

	@Override
	public boolean verifyDisappeared(WebElement ele) 
	{
		return false;

	}

	@Override
	public boolean verifyEnabled(WebElement ele) 
	{
		try 
		{
			if (ele.isEnabled()) 
			{
				System.out.println("The element " + ele + " is Enabled");
				log.info("The element " + ele + " is Enabled");
				return true;
			} 
			else 
			{
				System.out.println("The element " + ele + " is not Enabled");
				log.info("The element " + ele + " is not Enabled");
			}
		} 
		catch (WebDriverException e) 
		{
			System.out.println("WebDriverException : " + e.getMessage());
			log.error("WebDriverException : " + e.getMessage());
			return false;
		}
		return false;
	}

	@Override
	public boolean verifySelected(WebElement ele) 
	{
		try 
		{
			if (ele.isSelected()) 
			{
				System.out.println("The element " + ele + " is selected");
				return true;
			} 
			else 
			{
				System.out.println("The element " + ele + " is not selected");
			}
		} 
		catch (WebDriverException e) 
		{
			System.out.println("WebDriverException : " + e.getMessage());
			return false;
		}
		return false;
	}

	@Override
	public Boolean verifyNumberEquality(WebElement ele, int number) throws IOException, InterruptedException 
	{
		if (ele.getText().equals(number))
			return true;
		else
			return false;
	}

	@Override
	public WebElement locateElement(String locatorType, String value) 
	{
		try 
		{
			switch (locatorType.toLowerCase()) 
			{
			case "id":
				return driver.findElementById(value);
			case "name":
				return driver.findElementByName(value);
			case "class":
				return driver.findElementByClassName(value);
			case "link":
				return driver.findElementByLinkText(value);
			case "xpath":
				return driver.findElementByXPath(value);
			}
		}
		catch (NoSuchElementException e) 
		{
			System.err.println("The Element with locator:" + locatorType + " Not Found with value: " + value);
			throw new RuntimeException();
		}
		return null;
	}

	@Override
	public boolean verifyAlertNotPresent() 
	{
		try 
		{
			if (driver.switchTo().alert() != null) 
			{
				System.out.println("Here alert is present");
				return false;
			}
			else 
			{
				return true;
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Here alert is not presented");
		}
		return false;
	}

	@Override
	public boolean verifyAlertPresent() 
	{
		if (driver.switchTo().alert() != null) 
		{
			System.out.println("Here alert is present");
		}
		return true;
	}

	@Override
	public void verifyOptionalField(WebElement label) 
	{
		try 
		{
			if (!label.getText().contains("*")) 
			{
				log.info("Element " + label.getText() + " is optional field");
				reportStep("Element "+"'" + label.getText() + "'" + " is an optional field", "pass");
			}
			else 
			{
				reportStep("Element "+"'" + label.getText() + "'" + "is not an optional field", "fail");
				Assert.fail();
			}
		} 
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Element "+"'" + label.getText() + "'" + "is not an optional field", "fail");
			log.error("Element " +"'"+ label.getText() + "'" + "is not an optional field");
			Assert.fail();
		}
	}

	@Override
	public void verifyRequiredField(WebElement label) 
	{
		try 
		{
			if (label.getText().contains("*")) 
			{
				reportStep("Element "+"'" + label.getText()+ "'" + "is a required field", "pass");
				log.info("Element "+"'" + label.getText() + "'" + "is a required field");
			}
			else 
			{
				reportStep("Element "+"'" + label.getText() + "'" + "is not a required field, Asterisk(*) missing", "fail");
				Assert.fail();
			}
		} 
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Element "+"'" + label.getText() + "'" + "is not a required Field, Asterisk(*) missing" + error, "fail");
			log.error("Element "+"'" + label.getText() + "'" + "is not a required field, Asterisk(*) missing");
			Assert.fail();
		}
	}

	@Override
	public void verifyElementNotChecked(WebElement ele) 
	{
		try 
		{
			assertTrue(ele.isSelected());
			log.info("Element " +ele+ " is checked");
			reportStep("Element is checked", "pass");
		}
		catch (Exception e) 
		{
			log.error("Element " +ele+ " is not checked");
			reportStep("Element not checked", "fail");
			e.printStackTrace();
		}
	}

	@Override
	public int getTotalDropDownOptions(WebElement ele) 
	{
		try 
		{
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			/*
			 * wait = new WebDriverWait(driver, 10);
			 * wait.until(ExpectedConditions.elementToBeClickable(ele));
			 */
			Select dropdownoptions = new Select(ele);
			int totalOptions = dropdownoptions.getOptions().size() - 1;
			log.info("Total no.of dropdown options retrieved");
			reportStep("Total no.of dropdown options retrieved", "pass");
			System.out.println("Total no.of dropdown options: " + totalOptions);
			return totalOptions;

		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to get total dropdown options --> " + error, "fail");
			log.error("Unable to get total dropdown options "+ error);
			System.err.println("Unable to get total dropdown options");
			throw new RuntimeException();
		}
		finally 
		{
			takeSnap();
			log.info("Screenshot taken");
		}
	}

	@Override
	public void verifyCursorFocus(WebElement ele) throws IOException, InterruptedException 
	{
		Thread.sleep(3000);
		WebElement focusedElement = driver.switchTo().activeElement();
		if (focusedElement.equals(ele)) 
		{
			log.info("Cursor focus  in the " + ele);
			reportStep("Cursor focus  in the " + ele, "pass");
		} 
		else if (focusedElement != (ele)) 
		{
			System.out.println("username not in focus");
			reportStep("cursor focus not in the " + ele, "fail");
		}
		else 
		{

		}
	}

	@Override
	public void verifyUpdatedOn(WebElement ele,WebElement label) 
	{
		try 
		{
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			/*wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));*/
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Date date = new Date();
			String dateFormatted= dateFormat.format(date);
			log.info("Current date identified by the system");
			if(ele.getText().contains(dateFormatted))
			{
				log.info("Current date is correctly shown in "+label.getText());
				reportStep("Current Date is correctly shown in "+label.getText(), "pass"); 
			}
		} 
		catch (Exception e) 
		{
			int position =e.toString().indexOf("Exception:");
			String error = e.toString().substring(0,position+10);
			reportStep("Current Date is incorrectly shown in "+label.getText()+ "-->"+error, "fail");
			System.err.println("Current Date is incorrectly shown in "+label.getText());
			log.error("Current Date is incorrectly shown in "+label.getText());
			throw new RuntimeException();
		} 
		finally 
		{ 
			takeSnap();
			log.info("Screenshot taken");
		}


	}

	@Override
	public void verifyUpdatedBy(WebElement ele,WebElement label,WebElement eleloggedInUserID) 
	{
		try 
		{
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			/*wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.elementToBeClickable(ele));*/
			if(ele.getText().equals(eleloggedInUserID.getText()))
			{
				log.info("User ID identified in Banner is correctly shown in "+label.getText());
				reportStep("User ID is correctly shown in "+label.getText(), "pass"); 
			}
		} 
		catch (Exception e) 
		{
			int position =e.toString().indexOf("Exception:");
			String error = e.toString().substring(0,position+10);
			reportStep("User ID is incorrectly shown in "+label.getText()+ "-->"+error, "fail");
			System.err.println("User ID is incorrectly shown in "+label.getText());
			log.error("User ID identified in Banner is not shown in "+label.getText());
			throw new RuntimeException();
		} 
		finally 
		{ 
			takeSnap();
			log.info("Screenshot taken");
		}
	}


	public void SetGreaterFromdate(WebElement Fromdate, WebElement Todate)
	{

		setCurrentDate(Fromdate);
		setPastDate(Todate);		

	}

	
	@Override
	//@Test
	public void createHubforGrid() 
	{
		try 
		{
			File file = new File("./Resources/selenium-server-standalone-3.141.59.jar");
			log.info("Searching for selenium server jar file...");
			String directory = file.getParent();
			directory = file.getAbsoluteFile().getParent();
			log.info("Directory identified for the selenium server jar file");
			System.out.println("Selenium server location: " +directory);
			Runtime rt = Runtime.getRuntime();
			rt.exec("cmd.exe /c cd \""+directory+"\" & start java -jar selenium-server-standalone-3.141.59.jar -role hub");
			log.info("Successfully registered as Hub");
			InetAddress localhost = InetAddress.getLocalHost(); 
			String hubIPAddress = localhost.getHostAddress().trim();
			System.out.println("Hub IP Address : " +hubIPAddress); 


			//ProcessBuilder b = new ProcessBuilder("cmd", "/c", "C:\\Windows\\System32\\winrs.exe", "-r:http://192.168.1.184:5555", "-u:rajkumarprakasam", "-p:Seep2019", "H:\\KILL BROWSER.bat");


			/*try {            
	        	   ProcessBuilder launcher = new ProcessBuilder();
	        	   Map<String, String> environment = launcher.environment();
	        	   launcher.redirectErrorStream(true);
	        	   launcher.directory(new File("\\\\192.168.1.184\\TIERS\\DEV1\\RP\\VISUAL_BASIC\\"));

	        	   environment.put("name", "var");
	        	   launcher.command("your.exe");
	        	   Process p = launcher.start(); // And launch a new process

	        	} catch (Exception e){
	        	   e.printStackTrace();
	        	}*/


			Process p1 = Runtime.getRuntime().exec("cmd.exe /C pushd \\\\192.168.1.184   /U rajkumarprakasam /P Seep2019   \\H: && KILL BROWSER.bat && popd");

		} 
		catch (Exception e) 
		{
			int position =e.toString().indexOf("Exception:");
			String error = e.toString().substring(0,position+10);
			reportStep("Cannot create the hub -->"+error, "fail");
			System.err.println("Cannot create the hub");
			log.error("Cannot create the hub");
			throw new RuntimeException();
		} 

	}

	@Override
	//@Test
	public void joinAsNodeforGrid() 
	{
		try 
		{
			Runtime rt = Runtime.getRuntime();
			rt.exec("cmd /c start \"D:\\NVKIDS_V1 Workspace\\NVKIDS_SeleniumFramework\\Resources\\CallBatch.bat\"");

		} 
		catch (Exception e) 
		{
			int position =e.toString().indexOf("Exception:");
			String error = e.toString().substring(0,position+10);
			reportStep("Cannot create the Node -->"+error, "fail");
			System.err.println("Cannot create the Node");
			log.error("Cannot create the Node");
			throw new RuntimeException();
		} 

	}

	@Override
	public void setupGrid()
	{
		try
		{
			String URL = "http://www.DemoQA.com";
			String Node = "http://192.168.1.164:4444/wd/hub";
			DesiredCapabilities cap = DesiredCapabilities.firefox();
			driver = new RemoteWebDriver(new URL(Node), cap);
		}
		catch (Exception e) 
		{
			int position =e.toString().indexOf("Exception:");
			String error = e.toString().substring(0,position+10);
			reportStep("Cannot create the Node -->"+error, "fail");
			System.err.println("Cannot create the Node");
			log.error("Cannot create the Node");
			throw new RuntimeException();
		}

	}


	public void verifyFieldLength(WebElement ele, String data, int size) 	{

		int actualsize=0;

		try 
		{
			ele.sendKeys(data);
			String typedvalue = ele.getAttribute("value");
			actualsize = typedvalue.length();
			if(actualsize == size)
			{
				reportStep("The actual size matches the expected size " + actualsize, "pass");
				log.info("The actual size matches the expected size" + actualsize );
			}

			else
			{
				reportStep("The actual size does not matches the expected size, the Actual size is: " + actualsize, "fail");
				log.info("The actual size matches the expected size " + actualsize);
				Assert.fail();

			}
		}

		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("The actual size does not matches the expected size, the Actual size is: " + actualsize, "fail");
			log.info("The actual size matches the expected size " + actualsize);
			Assert.fail();
		}

	}

	@Override
	public int getRandomNumbers() 
	{
		Random rand = new Random();
		int n = rand.nextInt(9999);
		return n += 1;
	}

	public void pressEnterKey(WebElement ele)
	{

		ele.sendKeys(Keys.ENTER);


	}
	public void VerifyGridHasData(WebElement ele)
	{



		try

		{
			if (ele.getText().length()>0)
			{
				reportStep("The grid has data","pass");	
			}
			else
			{
				reportStep("The grid does not has any data","fail");

				Assert.fail();
			}




		}
		catch(Exception e)
		{
			reportStep("The grid does not has any data","fail");
			Assert.fail();

		}}


	@Override
	public void verifyGridHeaders(String...expected) {
		try 
		{
			ArrayList<String> Headers = new ArrayList<String>();
			ArrayList<String> expec = new ArrayList<String>();
			//List<WebElement> GridValues = driver.findElementsByXPath("//tr[@class='ng-star-inserted']/th");
			List<WebElement> GridValues = driver.findElementsByXPath("//tr[@class='ng-star-inserted']/th[not(contains(@class,'accordion-col'))]");
			
			for (WebElement col : GridValues){
				if(col.getAttribute("class").contains("hidden") == true ){
					continue;
				}
				else{
					Headers.add(col.findElement(By.tagName("label")).getText().trim());
					//System.out.println(col.findElement(By.tagName("label")).getText());
				}
			}
			for(String exp : expected ){
				expec.add(exp.trim());
				//System.out.println(exp);
			}

			if (expec.equals(Headers)) 
			{
				log.info("Grid Header is present");
				for(String display : expected){
					System.out.println(display);
					reportStep("Grid Header "+ display  +" is present", "pass");
				}


			}
			else{

				log.info("Grid Header is not present");
				reportStep("Grid Header is not present", "fail");
				Assert.fail();
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find header --> " + error, "fail");
			log.error("Unable to find headers --> " + error);
			System.err.println("Unable to find headers");
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}



	}

	@Override
	public void verifyInnerGridHeaders(String...expected) {
		try 
		{

			ArrayList<String> Headers = new ArrayList<String>();
			ArrayList<String> expec = new ArrayList<String>();
			List<WebElement> InnerGridHeaders = driver.findElementsByXPath("(//thead[@class='ui-table-thead'])[2]/tr[1]/th/label");
			for (WebElement col : InnerGridHeaders){

				Headers.add(col.getText().trim());
				//System.out.println(col.findElement(By.tagName("label")).getText());
			}

			for(String exp : expected ){

				expec.add(exp.trim());
				//System.out.println(exp);
			}


			if (expec.equals(Headers)) 
			{
				log.info("Grid Header is present");
				for(String display : expected){
					System.out.println(display);
					reportStep("Grid Header "+ display  +" is present", "pass");
				}


			}
			else{

				log.info("Grid Header is not present");
				reportStep("Grid Header is not present", "fail");
				Assert.fail();
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find header --> " + error, "fail");
			log.error("Unable to find headers --> " + error);
			System.err.println("Unable to find headers");
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}




	}

	public void VerifyChar_allowance(WebElement ele, String data)
	{

		try{
			ele.sendKeys(data);
			if (ele.getAttribute("value").equals(data))
			{
				reportStep(data+"allowed in the field","pass");	
			}
			else
			{
				reportStep(data+"not allowed in the field","fail");	
			}
		}
		catch(Exception e){
			reportStep(data+"not allowed in the field since cannot able to locate field","fail");
		}


	}


	public void VerifyChar_NotAllowance(WebElement ele, String data)
	{

		try{
			ele.sendKeys(data);
			if (ele.getAttribute("value").length()==0)
			{
				reportStep(data+"not allowed in the field","pass");	
			}
			else
			{
				reportStep(data+"allowed in the field","fail");	
			}
		}
		catch(Exception e){
			reportStep(data+"not allowed in the field since cannot able to locate field","fail");
		}


	}


	public void copypaste(WebElement ele, String text, int size) throws AWTException
	{
		int actualsize=0;
		try 
		{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

			clipboard.setContents(new StringSelection(text), null);
			ele.clear();
			ele.click();

			Robot robot = new Robot();

			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_V);



			String typedvalue = ele.getAttribute("value");

			if(text.equals(typedvalue)){
				reportStep("pasted text  matches the expected copied text " + actualsize, "pass");
				log.info("pasted text  matches the expected copied text" + actualsize );
			}

			else{
				reportStep("pasted text  does not matches the expected copied text, the Actual size is: " + actualsize, "fail");
				log.info("pasted text  matches the expected copied text " + actualsize);

			}
		}

		catch (Exception e) 
		{
			e.printStackTrace();
			reportStep("pasted text size does not matches the expected size, the Actual size is: " + actualsize, "fail");
			log.info("pasted text size matches the expected size " + actualsize);
		}
	}

	//@Test
	public static void selectQuery() throws SQLException, ClassNotFoundException { 

		String myUserName = "nvpocdbdev";
		String myPassword = "Protech370";


		String dbURL = "jdbc:db2://192.168.144.25:50000/NVKPOC";
		String username = myUserName;
		String password = myPassword;
		//Load DB2 JDBC Driver	
		Class.forName("com.ibm.db2.jcc.DB2Driver");
		//Creating connection to the database
		Connection con = DriverManager.getConnection(dbURL,username,password);
		//Creating statement object
		Statement st = con.createStatement();
		String selectquery = "SELECT * FROM PLIC_VW";
		//Executing the SQL Query and store the results in ResultSet
		ResultSet rs = st.executeQuery(selectquery);
		//While loop to iterate through all data and print results
		while (rs.next()) {
			System.out.println(rs.getString("LICENSE_ID"));
		}
		//Closing DB Connection
		con.close();
	}

	public void verifyInquiredGridResult( String data) {
		try 
		{
			WebElement exp;

			List<WebElement> ele = driver.findElementByXPath("//tr[@class='ng-star-inserted']/following::tbody").findElements(By.tagName("tr"));
			outerloop:
				for (WebElement row : ele){

					List<WebElement> cells = row.findElements(By.tagName("td"));

					for(WebElement cell : cells){

						String actualValue = cell.getText();
						if(actualValue.equals(data)){

							exp = cell;
							//doDoubleClick(cell);
							reportStep(actualValue + "Record  is available", "pass");
							log.info(actualValue + " Record  is available");
							break outerloop;
						}
						else if((ele.get(ele.size()-1)).equals(row)){
							reportStep(actualValue + "Record  is not available", "fail");
							log.info(actualValue + " Record not  is available");
							break outerloop;
						}
						else {
							continue;
						}
					}
				}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find the record --> " + error, "fail");
			log.error("Unable to find the record  --> " + error);
			System.err.println("Unable to find headers");
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}
	}

	public void doubleClickGridResult( String data) {
		try 
		{
			WebElement exp;

			List<WebElement> ele = driver.findElementByXPath("//tr[@class='ng-star-inserted']/following::tbody").findElements(By.tagName("tr"));
			outerloop:
				for (WebElement row : ele){

					List<WebElement> cells = row.findElements(By.tagName("td"));

					for(WebElement cell : cells){

						String actualValue = cell.getText();
						if(actualValue.equals(data)){

							exp = cell;
							doDoubleClick(cell);
							reportStep(actualValue + "Record  is available", "pass");
							log.info(actualValue + " Record  is available");
							break outerloop;
						}
						else if((ele.get(ele.size()-1)).equals(row)){
							reportStep(actualValue + "Record  is not available", "fail");
							log.info(actualValue.toString() + " Record not  is available");
							break outerloop;
						}
						else {
							continue;
						}
					}
				}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find the record --> " + error, "fail");
			log.error("Unable to fid the record  --> " + error);
			System.err.println("Unable to find headers");
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}
	}

	public void verifyErrorsInPanel(String... errors) 
	{
		try 
		{
			Map<String,String> expMap=new LinkedHashMap<>();
			for(int i=0;i<errors.length;i=i+2)
			{
				expMap.put(errors[i],errors[i+1]);
			}
			System.out.println(expMap);
			Map<String,String> actMap=new LinkedHashMap<>();
			List<WebElement> allPanelMessages = driver.findElementByXPath("//div[contains(@class,'errorInfoPanel')]").findElements(By.tagName("div"));
			List<WebElement> indivPanelMsgs = driver.findElementsByXPath("//div[contains(@class,'errorInfoPanelItem')]//div[contains(@class,'p-col-12 no-pad')]");
			for(int i=0;i<indivPanelMsgs.size();i=i+2)
			{
				for(int j=1;j<indivPanelMsgs.size();j=j+2)
				{	
					actMap.put(indivPanelMsgs.get(i).getText().substring(7),indivPanelMsgs.get(j).getText().substring(7));
				}
			}
			System.out.println(actMap);
			/*if(expMap.equals(actMap))
            		{
            			reportStep("Expected error messages are shown in the error panel", "pass");
            		}
            		else
            		{
            			reportStep("Expected error messages are not shown in the error panel", "fail");
            			Assert.fail();
            		}*/
			for (Entry<String,String> data : expMap.entrySet()) 
			{
				if(!actMap.containsKey(data.getKey()) || !actMap.containsValue(data.getValue()))
				{
					reportStep("'"+data.getKey()+" : " +data.getValue()+"'"+ " --> missing in error panel","fail");
					Assert.fail();	
				}
				else if(actMap.containsKey(data.getKey()) && actMap.containsValue(data.getValue()))
				{
					reportStep("'"+data.getKey()+" : " +data.getValue()+"'"+ " --> is present in error panel","pass");
				}
			}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable to access the error panel --> " + error, "fail");
			log.error("Unable to access the error panel  --> " + error);
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}
	}

	@Override
	public void verifyGridOptions(String... data) {

		try 
		{   

			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			wait =new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			ArrayList<String> GridOptions = new ArrayList<String>();
			ArrayList<String> expec = new ArrayList<String>();
			List<WebElement> GridValues = GridOptionList;
			for (WebElement col : GridValues){
				String s = col.getText().trim().replaceAll("[^a-z A-Z]", "");
				GridOptions.add(s.trim());

			}

			for(String exp : data ){
				expec.add(exp.trim());
				//System.out.println(exp);
			}

			if (expec.equals(GridOptions)) 
			{
				log.info("Grid Options present");
				for(String display : data){
					System.out.println(display);
					reportStep("Grid Options "+ display  +" is present", "pass");
				}


			}
			else{

				log.info("Grid Options is not present");
				reportStep("Grid Options is not present", "fail");
			}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}


	}

	@Override
	public void CheckShowFilters() {
		try 
		{   
			List<WebElement> SelectCheckBox = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]/p-checkbox/label");
			wait =new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfAllElements(SelectCheckBox));
			for(WebElement sel :SelectCheckBox ){

				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Show Filters")){
					reportStep("Grid Options "+ s.trim()  +" is present", "pass");

					if(!(sel.getAttribute("class").contains("ui-label-active"))){
						click(sel);
						log.info("Show Filter is selected");
						reportStep("Show Filter is selected", "pass");
					}
					else if(sel.getAttribute("class").contains("ui-label-active")){
						log.info("Show Filter is already selected");
						reportStep("Show Filter is already selected", "pass");
					}
					else{
						log.info("Grid Options is not present");
						reportStep("Grid Options is not present", "fail");
						Assert.fail("Grid options is not avaialbe");
					}

				}

				else{

					log.info("Grid Options is not present");
					reportStep("Grid Options is not present", "fail");
					Assert.fail("Grid options is not avaialbe");
				}
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}


	}

	@Override
	public void UnCheckShowFilters() {
		try 
		{   
			List<WebElement> SelectCheckBox = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]/p-checkbox/label");
			wait =new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfAllElements(SelectCheckBox));
			for(WebElement sel :SelectCheckBox ){
				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Show Filters")){
					reportStep("Grid Options "+ s.trim()  +" is present", "pass");

					if(!(sel.getAttribute("class").contains("ui-label-active"))){

						log.info("Show Filter is  already not selected");
						reportStep("Show Filter is already not selected", "pass");
					}
					else if(sel.getAttribute("class").contains("ui-label-active")){
						click(sel);
						log.info("Show Filter is unchecked sucessfully");
						reportStep("Show Filter is unchecked sucessfully", "pass");
					}
					else{
						log.info("Grid Options is not present");
						reportStep("Grid Options is not present", "fail");
						Assert.fail("Grid options is not avaialbe");
					}

				}

				else{

					log.info("Grid Options is not present");
					reportStep("Grid Options is not present", "fail");
					Assert.fail("Grid options is not avaialbe");
				}
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void clickResetFilter() {
		try{
			int count = 0;
			wait =new WebDriverWait(driver, 10);
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]/span");
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			for(WebElement sel :GridOptionList ){
				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				System.out.println(s);
				if(s.equals("Reset Filter")){
					click(sel);
					reportStep("Grid Options "+ s.trim()  +" is present and Clicked", "pass");
					break;
				}
				else if(count == (GridOptionList.size()-1)){
					log.info("Reset Filter is not present");
					reportStep("Reset Filter is not present", "fail");
					Assert.fail("Reset Filter is not avaialbe");

				}
				else{
					count = count + 1;
					continue;
				}
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}


	}

	@Override
	public void verifyShowColumns(String... data) {
		try{
			int count =0;
			//List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			WebElement GridOptionList = driver.findElementByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]/span[contains(text(),'Show Columns ')]");
			wait =new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOf(GridOptionList));

			List<String> ShowColumnsListAll = null;
			List<String> ShowColumnsListVisible = null;
			List<String> ShowColumnsListHidden = null;
			Actions action = new Actions(driver);
			click(GridOptionList);
		    action.clickAndHold(GridOptionList).build().perform();		
			reportStep("Show Columns  is present and clicked", "pass");
			List<WebElement> SelectCheckBox = driver.findElementsByXPath("//span[contains(text(),'Show Columns')]/following::p-checkbox/label");
			wait =new WebDriverWait(driver, 20);
			wait.until(ExpectedConditions.visibilityOfAllElements(SelectCheckBox));
			System.out.println("The size" + SelectCheckBox.size());
			
			for(WebElement col: SelectCheckBox){
				
				String s1 = col.getText().trim();
				System.out.println("Class"+ col.getAttribute("class"));
				
				/*ShowColumnsListAll.add(s1);
				if((col.getAttribute("class").contains("ui-label-active"))==false){
					ShowColumnsListHidden.add(col.getText().trim());
					count = count+1;
					continue;
					
				}
				else if(col.getAttribute("class").contains("ui-label-active")==true){
					ShowColumnsListVisible.add(col.getText().trim());
					count = count+1;
					continue;

				}
				else if(count ==(SelectCheckBox.size()-1)){
					break;
				}*/

			}




			if(data.equals(ShowColumnsListAll)){
				for(String visible:ShowColumnsListVisible  ){
					log.info(visible +" check box enabled");
					reportStep(visible+" check box enabled", "pass");

				}
				for(String hidden:ShowColumnsListHidden  ){
					log.info(hidden +" is hidden/unchecked");
					reportStep(hidden+" is hidden/unchecked", "pass");
				}
			}
			else{
				log.info("Show Column values are not matching");
				reportStep("Show Column values are not matching", "fail");
				Assert.fail("Show Column values are not matching");
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void selectShowColumn(String data) {
		try{
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			List<WebElement> SelectCheckBox = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]/p-checkbox/label");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(SelectCheckBox));
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			for(WebElement sel :GridOptionList ){

				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Show Columns")){
					click(sel);
					reportStep("Show Columns  is present", "pass");

					for(WebElement col: SelectCheckBox){
						String s1 = col.getText().trim().replaceAll("[^a-z A-Z]", "");
						if(s1.trim().equals(data)){
							if(!(col.getAttribute("class").contains("ui-label-active"))){
								click(sel);
								log.info("Show Filter is selected");
								reportStep("Show Filter is selected", "pass");

							}
							else if(col.getAttribute("class").contains("ui-label-active")){

								log.info("Show Filter is already selected");
								reportStep("Show Filter is already selected", "pass");

							}
							else{
								log.info(data + " column is not present");
								reportStep(data + " column is not present", "fail");
								Assert.fail(data + " column is not present");
							}
						}
					}
				}
			}
		}





		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void UnselectShowColumn(String data) {
		try{
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");

			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));

			for(WebElement sel :GridOptionList ){

				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Show Columns")){
					click(sel);
					reportStep("Show Columns  is present", "pass");
					List<WebElement> SelectCheckBox = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]/p-checkbox/label");
					wait.until(ExpectedConditions.visibilityOfAllElements(SelectCheckBox));
					for(WebElement col: SelectCheckBox){
						String s1 = col.getText().trim().replaceAll("[^a-z A-Z]", "");
						if(s1.trim().equals(data)){
							if(!(col.getAttribute("class").contains("ui-label-active"))){

								log.info("Show Filter is already not selected");
								reportStep("Show Filter is already not selected", "pass");

							}
							else if(col.getAttribute("class").contains("ui-label-active")){
								click(sel);
								log.info("Show Filter is unselected");
								reportStep("Show Filter is unselected", "pass");

							}
							else{
								log.info(data + " column is not present");
								reportStep(data + " column is not present", "fail");
								Assert.fail(data + " column is not present");
							}
						}
					}
				}
			}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void ClickExpandAll() {
		try{
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			for(WebElement sel :GridOptionList ){

				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Expand All")){
					click(sel);
					reportStep("Grid Expanded Sucessfully", "pass");
				}
				else if(s.trim().equals("Collapse All")){
					reportStep("Grid Already Expanded", "pass");
				}
				else{
					log.info("Could not expand the Grid");
					reportStep("Could not expand the Grid", "fail");
					Assert.fail("Could not expand the Grid");
				}
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}


	}

	@Override
	public void ClickCollapseAll() {
		try{
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			for(WebElement sel :GridOptionList ){

				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Collapse All")){
					click(sel);
					reportStep("Grid Collapsed Sucessfully", "pass");
				}
				else if(s.trim().equals("Expand All")){
					reportStep("Grid Already Collapsed", "pass");
				}
				else{
					log.info("Could not collpase the Grid");
					reportStep("Could not collpase the Grid", "fail");
					Assert.fail("Could not collpase the Grid");
				}
			}
		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void verifyDownloadFileOptions(String... data) {
		try{
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			List<String> ShowColumnsListAll = null;
			for(WebElement sel :GridOptionList ){
				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Download File")){
					click(sel);
					reportStep("Download File  is present", "pass");

					for(WebElement col: GridOptionList){
						String s1 = col.getText().trim().replaceAll("[^a-z A-Z]", "");
						if(s1.trim().equals("Download XLS") || s1.trim().equals("Download PDF") ){
							ShowColumnsListAll.add(s1);

						}
						else{
							continue;
						}

					}

					if(data.equals(ShowColumnsListAll)){
						for(String s2: ShowColumnsListAll){
							log.info(s2 + " Optional is available");
							reportStep(s2 + " Optional is available", "pass");

						}
					}
					else{
						log.info("Show Column values are not matching");
						reportStep("Show Column values are not matching", "fail");
						Assert.fail("Show Column values are not matching");

					}
				}
				else{

					log.info("Download File is not present");
					reportStep("Download File is not present", "fail");
					Assert.fail("Download File is not present");
				}
			}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void ClickAndDownloadFile(String data) {
		try{
			List<WebElement> GridOptionList = driver.findElementsByXPath("//div[@class='ui-overlaypanel-content']/div[contains(@class,'filterItems')]");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
			int count =0 ;
			for(WebElement sel :GridOptionList ){

				String s = sel.getText().trim().replaceAll("[^a-z A-Z]", "");
				if(s.trim().equals("Download File")){
					click(sel);
					reportStep("Download File  is present", "pass");

					for(WebElement col: GridOptionList){
						wait.until(ExpectedConditions.visibilityOfAllElements(GridOptionList));
						String s1 = col.getText().trim().replaceAll("[^a-z A-Z]", "");
						if(s1.trim().equals(data)){
							click(col);
							log.info(data + " is clicked to downlaod");
							reportStep(data + " is clicked to downlaod", "pass");
							count = count ++;
							break;

						}
						else if(count == (GridOptionList.size()-1)){
							log.info(data + " is not found");
							reportStep(data + " is not found", "fail");
							Assert.fail(data + " is not found");
						}
						else{
							count = count ++;
							continue;
						}

					}
				}

				else{

					log.info("Download File is not present");
					reportStep("Download File is not present", "fail");
					Assert.fail("Download File is not present");
				}
			}
		}

		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find grid options --> " + error, "fail");
			log.error("Unable to find grid options --> " + error);
			System.err.println("Unable to find grid options");
			Assert.fail("Unable to find grid options");
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void VerifyAssociatedScreensNavigatoin(String ScreenName, String SFName) {
		try 
		{
			int count=0;
			List<WebElement> GridValues = driver.findElementsByXPath("//li[contains(@class,'associatedMenuItem ')]/a/span[2]");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOfAllElements(GridValues));
			for (WebElement col : GridValues){
				if(col.getText().equals(ScreenName)){
					click(col);
					log.info(ScreenName + " is clicked to Navigate");
					reportStep(ScreenName + " is clicked to Navigate", "pass");
					verifyCurrenScreen(SFName);
					break;
				}

				else if(count == (GridValues.size()-1)){
					log.info(ScreenName + " is not found");
					reportStep(ScreenName + " is not found", "fail");
					Assert.fail(ScreenName + " is not found");

				}
				else {
					count = count++;
					continue;
				}

			}

		}
		catch (Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find Associated Screens --> " + error, "fail");
			log.error("Unable to fid Associated Screens --> " + error);
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}

	}

	@Override
	public void verifyCurrenScreen(String SFName) {
		try{
			WebElement eleScreen = driver.findElementByXPath("//p-breadcrumb/descendant::a/span[text()='"+SFName+"']");
			wait = new WebDriverWait(driver,10);
			wait.until(ExpectedConditions.visibilityOf(eleScreen));

			verifyExactText(eleScreen, SFName);
		}
		catch(Exception e) 
		{
			int position = e.toString().indexOf("Exception:");
			String error = e.toString().substring(0, position + 10);
			reportStep("Unable find Associated Screens --> " + error, "fail");
			log.error("Unable to fid Associated Screens --> " + error);
			Assert.fail();
		}
		finally 
		{
			takeSnap();
		}

	}





}
