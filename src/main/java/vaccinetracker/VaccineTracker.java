package vaccinetracker;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class VaccineTracker {
	WebDriver driver;
	public static final int SITE_CHECK_INTERVAL = 1000 * 60;
	public static final String SITE_URL = "https://epicproxy.et0502.epichosted.com/ucsd/SignupAndSchedule/EmbeddedSchedule?dept=9990995&id=99909951,99909952,99909953,99909954,99909955,99909956&vt=3550&view=plain&payor=-3#";
	public static final int LOADING_TIME = 5000;

	public static void main(String[] args) throws Exception {
		VaccineTracker v = new VaccineTracker();
		while (!v.start()) {
			v.shutdown();
			Thread.sleep(SITE_CHECK_INTERVAL);
		}
		v.alarm();
	}

	private void shutdown() {
		driver.close();
	}
	
	public boolean start() throws Exception {
		driver = initializeBrowser();
		driver.manage().window().maximize();
		driver.get(SITE_URL);
		Thread.sleep(LOADING_TIME);
		List<WebElement> spans = driver.findElements(By.tagName("span"));
		boolean noUpdates = true;
		for (WebElement span : spans) {
			if (span.isDisplayed()) {
				System.out.println(span.getText());
				if (span.getText()
						.contains("Sorry, we couldn't find any open appointments. Please check back later.")) {
					System.out.println("no updates :(");
					noUpdates = false;
				}
			}
		}
		return noUpdates;
	}

	public WebDriver initializeBrowser() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("start-maximized");
		options.addArguments("enable-automation");
		options.addArguments("--window-size=1920,1080");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-browser-side-navigation");
		options.addArguments("--disable-gpu");
		options.addArguments("--headless");
		return new ChromeDriver(options);
	}

	void alarm() throws Exception {
		final int alarmInterval = 1000;
		while (true) {
			Toolkit.getDefaultToolkit().beep();
			Thread.sleep(alarmInterval);
		}
	}

	/**
	 * Only works on Mac
	 * 
	 * @throws IOException
	 */
	void openBrowser() throws IOException {
		Runtime rt = Runtime.getRuntime();
		rt.exec("open " + SITE_URL);
	}
}
