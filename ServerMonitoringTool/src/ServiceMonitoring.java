import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will be used to monitor services and its status. 
 * Services can be tomcat service, MySQL service etc 
 * @author Ragini Mahajan
 * @Date 14th Nov 2018
 */
public class ServiceMonitoring {

	List<String> servicesNameToMonitor;

	public ServiceMonitoring(List<String> servicesNameToMonitor){
		this.servicesNameToMonitor = new ArrayList<String>(servicesNameToMonitor);
	}
	
	
	public boolean validatedEmailTrigger(Map<String, String> serviceNamesStatus, String triggerPointConstaint){
		if(triggerPointConstaint != null){
			for (String serviceName : servicesNameToMonitor) {
				if(serviceName != null && serviceNamesStatus.get(serviceName) != null && serviceNamesStatus.get(serviceName).contains(triggerPointConstaint)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * This method will check service staus against list provided while creating object.
	 * Display status in html format
	 * @param serviceNamesStatus- Map holding service name as key and status as value
	 * @return Services information in HTML format
	 */
	public String displayServicesStatusInHTML(Map<String, String> serviceNamesStatus){
		
		StringBuilder emailMessage = new StringBuilder("");
		emailMessage.append("	<tr>");
		emailMessage.append("		<td align=\"center\"><b><font size=\"4\">Service Monitoring</font></b> </td>");
		emailMessage.append("	</tr>");

		emailMessage.append("	<tr>");
		emailMessage.append("		<td>");
		emailMessage.append("			<table style=\"border-spacing: 0px;border-collapse: collapse; border:1px solid black\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
		emailMessage.append("				<tr style=\"border-bottom: 1px solid black;\">");
		emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black;\"><b>Service Name</b></td>");
		emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black;\"><b>Status </b></td>");
		emailMessage.append("				</tr>");
		
		for (String serviceName : servicesNameToMonitor) {
			if(serviceName != null){
				emailMessage.append("		<tr>");
				emailMessage.append("			<td align=\"center\" style=\"border: 1px solid black;\">"+ serviceName +"</td>");
				if(serviceNamesStatus.get(serviceName) != null) {
					//If service is stopped show in red color
					if(serviceNamesStatus.get(serviceName).contains("STOPPED")) {
						emailMessage.append("	<td style=\"color:#ff0000; border-right:1px solid black\">"+serviceNamesStatus.get(serviceName)+"</td>");
					}
					else {
						emailMessage.append("	<td align=\"center\" style=\"border: 1px solid black;\">"+serviceNamesStatus.get(serviceName)+"</td>");
					}
				}
				else {
					//If service is not present 
					emailMessage.append("		<td align=\"center\" style=\"border: 1px solid black;\">SERVICE NOT PRESENT</td>");
				}
				emailMessage.append("		</tr>");
			}
		}
		emailMessage.append("			</table>");
		emailMessage.append("		</td>");
		emailMessage.append("	</tr>");
		
		return emailMessage.toString();
	}

	/**
	 * Get current status of service and create map of that will hold service name and its status
	 * @return Map with key as Service name and value as service status
	 */
	public Map<String, String> serviceMonitoringAnalyzer() {
		Map<String, String> serviceNamesStatus = new HashMap<String, String>();
		for(String servicesName : servicesNameToMonitor) {
			String readLine = "";
			try {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec("sc query \""+servicesName+"\"");
				
				StringBuffer line = new StringBuffer();
				BufferedReader input =  new BufferedReader (new InputStreamReader(proc.getInputStream()));
				while((readLine = input.readLine()) != null) {
					if(readLine.contains("STATE")){
						line.append(readLine);	
						break;
					}
				}

				String serviceStatusStatement = line.toString().trim();
				if(serviceStatusStatement.length() > 0){
					serviceStatusStatement = serviceStatusStatement.substring(serviceStatusStatement.indexOf(":")).trim();
					serviceStatusStatement = serviceStatusStatement.substring(serviceStatusStatement.lastIndexOf(" ")).trim();
					String[] serviceStatus = serviceStatusStatement.split(" ");
					serviceNamesStatus.put(servicesName, serviceStatus[0].trim());
				}
			}
			catch(IOException e) {
				System.out.println(e);
			}
		}
		return serviceNamesStatus;
	}
}
