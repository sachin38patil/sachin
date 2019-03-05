import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//calculate average CPU utilization
public class CpuUtilizationAnalyzer extends Thread {
	
	public double totalCpuAverage = 0.0;
	public boolean continueLoop = true;
	int monitoringTimeInSeconds = 10;
	
	public CpuUtilizationAnalyzer(String monitoringTimeInSeconds) {
		if(monitoringTimeInSeconds != null && !monitoringTimeInSeconds.isEmpty() && !monitoringTimeInSeconds.equals("0")){
			this.monitoringTimeInSeconds = Integer.parseInt(monitoringTimeInSeconds);
		}
	}

	// Single CPU with Single/Multi core processors
	List<Integer> cpuUsagePercentageList = new ArrayList<Integer>();

	// Multi CPU with Single/Multi core processors
	Map<String, Double> cpuDeviceIdLoadPercentageAverageMap = new TreeMap<String, Double>();
	Map<String, String> cpuDeviceIdNamesMap = new TreeMap<String, String>();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(totalCpuAverage);
		return sb.toString(); 
	}

	/**
	 * This method will check service staus against list provided while creating object.
	 * Display status in html format
	 * @param serviceNamesStatus- Map holding service name as key and status as value
	 * @return Services information in HTML format
	 */
	public String displayCpuUtilizationInHTML(Double triggerPoint){
		
		String borderRightTD = "style=\"border:1px solid black\"";
		
		StringBuilder resultHTML = new StringBuilder();
		resultHTML.append("	<tr>");
		resultHTML.append("		<td align=\"left\">");

		resultHTML.append("			<table style=\"border-spacing: 0px;border-collapse: collapse; border:1px solid black\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
		resultHTML.append("				<tr style=\"border-bottom: 1px solid black;\">");
		resultHTML.append("					<td align=\"center\" "+ borderRightTD +"><b>Processor</b></td>");
		resultHTML.append("					<td align=\"center\" "+ borderRightTD +"><b>Average</b></td>");
		resultHTML.append("				</tr>");
		for(Map.Entry<String, Double> cpuDeviceIdLoadPercentage : cpuDeviceIdLoadPercentageAverageMap.entrySet()) {
			String processorName = cpuDeviceIdNamesMap.get(cpuDeviceIdLoadPercentage.getKey());
			Double processorAverage = 0.0;
			if(cpuDeviceIdLoadPercentage.getValue() > 0){
				BigDecimal bd = new BigDecimal(cpuDeviceIdLoadPercentage.getValue()).setScale(3, RoundingMode.HALF_EVEN);
				processorAverage = bd.doubleValue();
			}
			String cssStyleCPU = "";
			if(triggerPoint <= cpuDeviceIdLoadPercentage.getValue()){
				cssStyleCPU = "color:#ff0000";
			}
			resultHTML.append("				<tr style=\""+ cssStyleCPU +"\">");
			resultHTML.append("					<td align=\"center\" "+ borderRightTD +"><b>"+processorName+"</b></td>");
			resultHTML.append("					<td align=\"center\" "+ borderRightTD +"><b>"+processorAverage+" %</b></td>");
			resultHTML.append("				</tr>");
		}
		resultHTML.append("			</table>");
		resultHTML.append("		</td>");
		resultHTML.append("	</tr>");

		return resultHTML.toString();
	}

	public void run(){

		Map<String, List<Double>> cpuDeviceIdLoadPercentageMap = new TreeMap<String, List<Double>>();
		for(short iteration = 0; iteration < monitoringTimeInSeconds; iteration++) {
			try {

				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec("wmic cpu get DeviceID, loadpercentage, Name");
				String readLine = null;
				BufferedReader input =  new BufferedReader (new InputStreamReader(proc.getInputStream()));
				input.readLine();

				while ((readLine = input.readLine()) != null) {
					if(!readLine.trim().isEmpty()){
						// lineAttributes will contains [DeviceID, loadpercentage, Name]
						String lineAttributes[] = readLine.split("\\s{2,}");
						if(lineAttributes.length == 3){
							String deviceID = lineAttributes[0].trim();
							String loadPercentage = lineAttributes[1].trim();
							String Name = lineAttributes[2].trim();

							cpuDeviceIdNamesMap.put(deviceID, Name);

							List<Double> cpuLoadPercentagePerSecond = null;
							if(cpuDeviceIdLoadPercentageMap.containsKey(deviceID)){
								cpuLoadPercentagePerSecond = cpuDeviceIdLoadPercentageMap.get(deviceID);
							}else{
								cpuLoadPercentagePerSecond = new ArrayList<Double>();
							}
							cpuLoadPercentagePerSecond.add(new Double(loadPercentage));
							cpuDeviceIdLoadPercentageMap.put(deviceID, cpuLoadPercentagePerSecond);
						}
					}
				}

				input.close();
				
				Thread.sleep(1000);
			}
			catch(IOException e) {
				System.out.println(e);
			}
			catch(InterruptedException e)
			{
				System.out.println(e);
			}  
		}


		// Average CPU = Average of (Average of processor 1, processor 2 and so on )
		for(Map.Entry<String, List<Double>> cpuDeviceIdLoadPercentage : cpuDeviceIdLoadPercentageMap.entrySet()) {
			double averagePerProcessor = 0.0;
			if(cpuDeviceIdLoadPercentage.getValue() != null && cpuDeviceIdLoadPercentage.getValue().size() != 0){
				for(Double loadPercentage : cpuDeviceIdLoadPercentage.getValue()){
					averagePerProcessor = averagePerProcessor + loadPercentage;
				}
				averagePerProcessor = averagePerProcessor / cpuDeviceIdLoadPercentage.getValue().size();
			}
			totalCpuAverage = totalCpuAverage + averagePerProcessor;
			cpuDeviceIdLoadPercentageAverageMap.put(cpuDeviceIdLoadPercentage.getKey(), averagePerProcessor);
		}

		if(cpuDeviceIdLoadPercentageAverageMap != null && !cpuDeviceIdLoadPercentageAverageMap.isEmpty()){
			totalCpuAverage = totalCpuAverage / cpuDeviceIdLoadPercentageAverageMap.size();
			BigDecimal bd = new BigDecimal(totalCpuAverage).setScale(3, RoundingMode.HALF_EVEN);
			totalCpuAverage = bd.doubleValue();
		}
		continueLoop = false;
	}
}