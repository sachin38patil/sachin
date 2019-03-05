import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RamUsage extends Thread
{
	int monitoringTimeInSeconds = 10;
	boolean continueLoop = true;
	
	// Properties required to display
	int totalPhysicalMemory = 0;
	public double ramUsagePercentageAverage = 0.0;

	// Constructor
	public RamUsage(String monitoringTimeInSeconds){
		if(monitoringTimeInSeconds != null && !monitoringTimeInSeconds.isEmpty() && !monitoringTimeInSeconds.equals("0")){
			this.monitoringTimeInSeconds = Integer.parseInt(monitoringTimeInSeconds);
		}
	}
	
	
	public void run(){
		
		List<Double> ramUsageInPercentageList = new ArrayList<Double>();
		for(short iteration = 0; iteration < monitoringTimeInSeconds; iteration++) {
			try {
				double ramUsagePercentage = this.getRamUsagePercentage();
				ramUsageInPercentageList.add(ramUsagePercentage);
				Thread.sleep(1000);
			}
			catch(IOException e) {
				System.out.println(e);
			}
			catch(InterruptedException e) {
				System.out.println(e);
			}
		}
		
		if(ramUsageInPercentageList != null && !ramUsageInPercentageList.isEmpty()){
			double ramUsagePercentageAverage = 0.0;
			for(Double ramUsage : ramUsageInPercentageList){
				ramUsagePercentageAverage = ramUsagePercentageAverage + ramUsage; 
			}
			ramUsagePercentageAverage = ramUsagePercentageAverage / ramUsageInPercentageList.size();
			BigDecimal bd = new BigDecimal(ramUsagePercentageAverage).setScale(2, RoundingMode.HALF_EVEN);
			this.ramUsagePercentageAverage = bd.doubleValue();
		}
		continueLoop = false;
	}
	
	
	//method calculate RAM usage in percentage
	public double getRamUsagePercentage() throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("systeminfo");
		String readLine;
		int totalPhysicalMemory = -1;
		int availablePhysoicalMemory = -1;
		BufferedReader commandOutput =  new BufferedReader (new InputStreamReader(proc.getInputStream()));
		while ((readLine = commandOutput.readLine()) != null) {
			if(readLine.contains("Total Physical Memory")) {
				String totalSplitted[] = readLine.split(" ");
				totalPhysicalMemory = Integer.valueOf(totalSplitted[7].replace(",", "").trim());
			}
			if(readLine.contains("Available Physical Memory")) {
				String availableSplitted[]= readLine.split(" ");
				availablePhysoicalMemory = Integer.valueOf(availableSplitted[3].replace(",", ""));
			}
			if(totalPhysicalMemory != -1 && availablePhysoicalMemory != -1){
				break;
			}
		}	
		this.totalPhysicalMemory = new Integer(totalPhysicalMemory);

		double ramUsageMemory = totalPhysicalMemory - availablePhysoicalMemory;
		double ramUsagePercentage = (ramUsageMemory/totalPhysicalMemory)*100;
		BigDecimal bd = new BigDecimal(ramUsagePercentage).setScale(2, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}
}