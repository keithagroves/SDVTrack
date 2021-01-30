package vaccinetracker;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
	public static final int LOADING_TIME = 10000;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				VaccineTracker v = new VaccineTracker();
				v.createUI();
			}
		});

	}

	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	JLabel label = new JLabel("last checked: none");
	void createUI() {
		frame.setTitle("Vaccine Appointment Tracker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.add(panel);
		panel.setLayout(new GridLayout(3,1));
		JButton button = new JButton("start");
		panel.add(button);
		panel.add(label);
		button.addActionListener(e -> {
			button.setText("seaching for an available appointment. This will take a while...");
			button.setEnabled(false);
			new Thread(() -> {
				try {
					this.run();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(frame, "Error", "Error", JOptionPane.ERROR_MESSAGE, null);
				}
			}).start();

		});
		frame.pack();

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
		boolean updates = true;
		for (WebElement span : spans) {
			if (span.isDisplayed()) {
				System.out.println(span.getText());
				if (span.getText()
						.contains("Sorry, we couldn't find any open appointments. Please check back later.")) {
					System.out.println("no updates :(");
					updates = false;
				}
			}
		}
		label.setText("last checked: "+ DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(ZonedDateTime.of(LocalDateTime.now(),ZoneId.of("America/Los_Angeles"))));
		frame.pack();
		return updates;
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

	void run() throws Exception {
		long lastStartTime = System.currentTimeMillis();
		while (!this.start()) {
			this.shutdown();
			long timeLeft = Math.max(SITE_CHECK_INTERVAL - (System.currentTimeMillis()-lastStartTime), 0);
			Thread.sleep(timeLeft);
			lastStartTime = System.currentTimeMillis();
		}
		try {
			this.openBrowser();
		} catch (Exception e) {
			System.err.print("this only works on mac.");
		}
		this.alarm();
	}
}
