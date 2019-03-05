import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This class holds server monitoring constraints and validate with validateConstraint method.
 * @author Onkar Paranjape
 *
 */
public class ServerMonitoringConstraints {

	//Read file to get constraint values
	String 			clientName = null;
	String 			serverName = null;
	String			diskUsedSpacePercentage = null;
	String			cpuUsagePercentage = null;
	String			ramUsagePercentage = null;
	String			tomcatLogsFolderSizeMB = null;
	List<String> 	servicesNameToMonitor = new ArrayList<String>();
	String	 		emailHost = null;
	String	 		emailPort = null;
	String 			emailFromUsername = null;
	String 			emailFromPassword = null;
	List<String> 	emailsTo =  new ArrayList<String>();
	String 			emailToHostType = null;
	String 			mySqlBackupPath = null;
	String 			remainderEmailTriggerInterval = null;
	String 			cpuMonitoringTimeInSeconds = null;
	String 			ramMonitoringTimeInSeconds = null;
	
	List<String>	scheduledReportEmailTo = new ArrayList<String>();

	public ServerMonitoringConstraints() throws IOException{
		// Reading
		FileInputStream fileInputStream = new FileInputStream("ConstraintSpecification.txt"); 
		Properties propConstraints = new Properties();
		propConstraints.load(fileInputStream);

		if(propConstraints.getProperty("Client_Name") != null) {
			clientName = propConstraints.getProperty("Client_Name");
		}
		if(propConstraints.getProperty("Server_Name") != null) {
			serverName = propConstraints.getProperty("Server_Name");
		}
		if(propConstraints.getProperty("Disk_Used_Space_Percentage") != null) {
			diskUsedSpacePercentage = propConstraints.getProperty("Disk_Used_Space_Percentage");
		}
		if(propConstraints.getProperty("CPU_Usage_Percentage") != null) {
			cpuUsagePercentage = propConstraints.getProperty("CPU_Usage_Percentage");
		}
		if(propConstraints.getProperty("RAM_Usage_Percentage") != null) {
			ramUsagePercentage = propConstraints.getProperty("RAM_Usage_Percentage");
		}
		if(propConstraints.getProperty("Tomcat_Logs_Folder_Size_MB") != null){
			tomcatLogsFolderSizeMB = propConstraints.getProperty("Tomcat_Logs_Folder_Size_MB");
		}
		if(propConstraints.getProperty("Service_Names_Comma_Separated") != null) {
			String [] serviceNameArray = propConstraints.getProperty("Service_Names_Comma_Separated").split(",");
			if(serviceNameArray != null && serviceNameArray.length > 0){
				servicesNameToMonitor.addAll(Arrays.asList(serviceNameArray));
			}
		}
		if(propConstraints.getProperty("Email_Host") != null) {
			emailHost = propConstraints.getProperty("Email_Host");
		}
		if(propConstraints.getProperty("Email_Port") != null) {
			emailPort = propConstraints.getProperty("Email_Port");
		}
		if(propConstraints.getProperty("Email_From_Username") != null) {
			emailFromUsername = propConstraints.getProperty("Email_From_Username");
		}
		if(propConstraints.getProperty("Email_From_Password") != null) {
			emailFromPassword = propConstraints.getProperty("Email_From_Password");
		}
		if(propConstraints.getProperty("Email_To") != null) {
			String [] emailsToArray = propConstraints.getProperty("Email_To").split(",");
			if(emailsToArray != null && emailsToArray.length > 0){
				emailsTo.addAll(Arrays.asList(emailsToArray));
			}
		}
		if(propConstraints.getProperty("Email_To_Host_Type") != null) {
			emailToHostType = propConstraints.getProperty("Email_To_Host_Type");
		}

		if(propConstraints.getProperty("MySQL_Backup_Files_path") != null) {
			mySqlBackupPath = propConstraints.getProperty("MySQL_Backup_Files_path");
		}

		if(propConstraints.getProperty("Remainder_Email_Trigger_interval_Minutes") != null) {
			remainderEmailTriggerInterval = propConstraints.getProperty("Remainder_Email_Trigger_interval_Minutes");
		}

		if(propConstraints.getProperty("CPU_Monitoring_Time_In_Seconds") != null) {
			cpuMonitoringTimeInSeconds = propConstraints.getProperty("CPU_Monitoring_Time_In_Seconds");
		}
		
		if(propConstraints.getProperty("Ram_Monitoring_Time_In_Seconds") != null) {
			ramMonitoringTimeInSeconds = propConstraints.getProperty("RAM_Monitoring_Time_In_Seconds");
		}
		
		if(propConstraints.getProperty("Scheduled_Report_Email_To") != null) {
			String [] emailsToArray = propConstraints.getProperty("Scheduled_Report_Email_To").split(",");
			if(emailsToArray != null && emailsToArray.length > 0){
				scheduledReportEmailTo.addAll(Arrays.asList(emailsToArray));
			}
		}

		fileInputStream.close();
		System.out.println("Reading inputs...");
		LoggerUtils.appendLog("Reading inputs...");
	}
	
	public boolean isValidConstaints(){
		// Validate mandatory inputs
		if(clientName == null || clientName.isEmpty()){
			return false;
		}
		if(emailsTo == null || emailsTo.isEmpty()){
			return false;
		}
		if(emailHost == null || emailHost.isEmpty()){
			return false;
		}
		if(emailPort == null || emailPort.isEmpty()){
			return false;
		}
		if(emailFromUsername == null || emailFromUsername.isEmpty()){
			return false;
		}

		if(emailToHostType == null || !emailToHostType.equals("exchange")){
			if(emailFromPassword == null || emailFromPassword.isEmpty()){
				return false;
			}
		}
		return true;
	}

	public String getClientName() {
		return clientName;
	}

	public String getServerName() {
		return serverName;
	}

	public String getDiskUsedSpacePercentage() {
		return diskUsedSpacePercentage;
	}

	public String getCpuUsagePercentage() {
		return cpuUsagePercentage;
	}

	public String getRamUsagePercentage() {
		return ramUsagePercentage;
	}

	public String getTomcatLogsFolderSizeMB() {
		return tomcatLogsFolderSizeMB;
	}

	public List<String> getServicesNameToMonitor() {
		return servicesNameToMonitor;
	}

	public String getEmailHost() {
		return emailHost;
	}

	public String getEmailPort() {
		return emailPort;
	}

	public String getEmailFromUsername() {
		return emailFromUsername;
	}

	public String getEmailFromPassword() {
		return emailFromPassword;
	}

	public List<String> getEmailsTo() {
		return emailsTo;
	}
	
	public String getEmailToHostType() {
		return emailToHostType;
	}

	public String getMySqlBackupPath() {
		return mySqlBackupPath;
	}

	public String getRemainderEmailTriggerInterval() {
		return remainderEmailTriggerInterval;
	}

	public String getCpuMonitoringTimeInSeconds() {
		return cpuMonitoringTimeInSeconds;
	}

	public List<String> getScheduledReportEmailTo() {
		return scheduledReportEmailTo;
	}

	public String getRamMonitoringTimeInSeconds() {
		return ramMonitoringTimeInSeconds;
	}
}
