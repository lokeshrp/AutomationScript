package com.demo;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.demo.data.ReportDTO;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DemoDataGatherMain {
	private static final String CSV_SEPARATOR = ",";

	public static void main(String[] args) throws Exception {

		String inputFile = null;
		String numberOfBrowser = "1";
		String outPutFilePath = "C:/";
		if (args.length == 0 && args.length < 3) {
			throw new Exception("Error in input file or path");
		}

		inputFile = args[0];
		numberOfBrowser = args[2];
		outPutFilePath = args[1];
		// System Property for Chrome Driver
		/*
		 * System.setProperty("webdriver.chrome.driver",
		 * "C:\\Users\\Lokesh\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe"
		 * );
		 */
		WebDriverManager.chromedriver().setup();
		FileInputStream fs = new FileInputStream(inputFile);
		// Creating a workbook
		// XSSFWorkbook wb = new XSSFWorkbook(new File("file.xlsx"));
		XSSFWorkbook workbook = new XSSFWorkbook(fs);
		Sheet sheet = workbook.getSheetAt(0);
		List<ReportDTO> finalResult = new ArrayList<>();
		List<ReportDTO> errorDetails = new ArrayList<>();
		List<ReportDTO> inputData = new ArrayList<>();

		Map<String, ReportDTO> map = new HashMap<>();
		for (Row row : sheet) {
			Cell cell1 = row.getCell(0);
			ReportDTO reportDTO = new ReportDTO();
			reportDTO.setPmkId(cell1.getStringCellValue());
			inputData.add(reportDTO);
		}
		// Instantiate a ChromeDriver class.
		List<Callable<ReportDTO>> tasks = new ArrayList<>();
		for (ReportDTO reportDTO : inputData) {
			WebDriver driver = null;
			Callable<ReportDTO> callable = new Callable<ReportDTO>() {
				@Override
				public ReportDTO call() throws Exception {
					return runScript(driver, reportDTO);
				}
			};
			tasks.add(callable);
		}

		ExecutorService service = Executors.newFixedThreadPool(Integer.valueOf(numberOfBrowser));
		try {
			List<Future<ReportDTO>> results = service.invokeAll(tasks, 400, TimeUnit.MINUTES);
			for (Future<ReportDTO> result : results) {
				ReportDTO reportDTO = result.get();
				if (reportDTO.isSucess()) {
					finalResult.add(reportDTO);
				} else {
					errorDetails.add(reportDTO);
				}
			}
		} catch (Exception e) {
			System.out.println("error in execution of script "+e.getMessage());
		}

		writeToCSV(finalResult, true, outPutFilePath);
		writeToCSV(errorDetails, false, outPutFilePath);
		System.out.println("****************end of the Program***********");
		System.exit(0);
	}

	private static ReportDTO runScript(WebDriver driver, ReportDTO reportInputData) {
		try {
			driver = new ChromeDriver();

			driver.navigate().to("https://fruitspmk.karnataka.gov.in/MISReport/CheckStatus.aspx");
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			String pmkId = reportInputData.getPmkId();
			ReportDTO reportDTO = new ReportDTO();
			// Maximize the browser
			driver.manage().window().maximize();
			Thread.sleep(1000);
			// Scroll down the webpage by 5000 pixels
			JavascriptExecutor js = (JavascriptExecutor) driver;
			// js.executeScript("scrollBy(0, 5000)");
			WebElement findElement = driver.findElement(By.id("ContentPlaceHolder1_rdoSearchType_0"));
			findElement.click();
			Thread.sleep(1000);
			WebElement searchKey = driver.findElement(By.id("ContentPlaceHolder1_txtFID"));
			searchKey.sendKeys(pmkId);
			Thread.sleep(1000);
			WebElement submitButton = driver.findElement(By.id("ContentPlaceHolder1_BtnSearch"));
			submitButton.click();
			Thread.sleep(3000);
			WebElement goire = driver.findElement(By.id("ContentPlaceHolder1_lblgoiregno"));
			String goireNum = goire.getText();
			System.out.println("goire " + goireNum);
			WebElement nameElement = driver.findElement(By.id("ContentPlaceHolder1_lblName"));
			String name = nameElement.getText();

			boolean isExist = driver.findElements(By.id("ContentPlaceHolder1_GridIneligibleReason")).isEmpty();
			if (!isExist) {
				WebElement table = driver.findElement(By.id("ContentPlaceHolder1_GridIneligibleReason"));
				List<WebElement> rows = table.findElements(By.xpath(".//tr"));
				for (WebElement row : rows) {
					List<WebElement> cells = row.findElements(By.xpath(".//td"));
					if (!cells.isEmpty()) {
						String fidNum = cells.get(1).getText();
						reportDTO.setFidNumber(fidNum);
						break;
					}
				}
			} else {
				WebElement grdPaymenttable = driver.findElement(By.id("ContentPlaceHolder1_grdPayment"));
				List<WebElement> rows = grdPaymenttable.findElements(By.xpath(".//tr"));
				for (WebElement row : rows) {
					List<WebElement> cells = row.findElements(By.xpath(".//td"));
					if (!cells.isEmpty()) {
						String fidNum = cells.get(0).getText();
						reportDTO.setFidNumber(fidNum);
						break;
					}
				}
			}
			Thread.sleep(1000);
			reportDTO.setPmkId(pmkId);
			reportDTO.setName(name);
			reportDTO.setGoiRegNo(goireNum);
			reportDTO.setSucess(true);
			// driver.close();
			driver.quit();
			return reportDTO;
		} catch (Exception e) {
			ReportDTO reportDTO = new ReportDTO();
			reportDTO.setPmkId(reportInputData.getPmkId());

			reportDTO.setSucess(false);
			// driver.close();
			driver.quit();
			return reportDTO;
		}
	}

	private static void writeToCSV(List<ReportDTO> finalResult, boolean isSuccess, String outPutFilePath) {
		try {
			String filePath = "FinalReport.csv";
			if (!isSuccess) {
				filePath = "ErrorReport.csv";
			}
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outPutFilePath + filePath), "UTF-8"));
			for (ReportDTO reportDTO : finalResult) {
				StringBuffer oneLine = new StringBuffer();
				oneLine.append(reportDTO.getPmkId());
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(reportDTO.getName());
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(reportDTO.getFidNumber());
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(reportDTO.getGoiRegNo());
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();

		} catch (Exception e) {
			System.out.println("error in exporting file " + e.getMessage());
		}
	}
}