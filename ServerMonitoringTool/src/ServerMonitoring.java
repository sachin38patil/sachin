import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 * The ServerMonitoringTest class check server status in regards to v
 * System information and Services monitoring. 
 * 
 * It will also trigger email if parameter constraints are been set.
 * @author Onkar Paranjape
 *
 */
public class ServerMonitoring {

	static long lastErrorEmailTriggeredOn = -1L;

	/**
	 * Method will check CPU utilization, RAM usages, Disk Space Analyzer etc
	 * @throws AddressException : Email from constraint not found or invalid
	 * @throws MessagingException : Email from constraint not found or invalid
	 * @throws IOException : Error while reading file
	 * @throws InterruptedException
	 */
	public static void performServerMonitoring(boolean calledByReportingScheduler) throws MessagingException, IOException, InterruptedException {

		if(calledByReportingScheduler){
			System.out.println("Program execution started for reporting email...");
			LoggerUtils.appendLog("\n");
			LoggerUtils.appendLog("Program execution started for reporting email...");
		}else{
			System.out.println("Program execution started...");
			LoggerUtils.appendLog("\n");
			LoggerUtils.appendLog("Program execution started...");
		}
		

		ServerMonitoringConstraints constraints = new ServerMonitoringConstraints();
		if(!constraints.isValidConstaints()){
			System.out.println("Stoped execution, Missing constraints values");
			LoggerUtils.appendLog("Stoped execution, Missing constraints values");
			return;
		}
		System.out.println("Reading completed");
		LoggerUtils.appendLog("Reading completed");

		boolean triggerEmailNotification = false;

		// Checking for disk space
		double triggerPoint = 0.0;
		System.out.println("Checking for disk space");
		LoggerUtils.appendLog("Checking for disk space");
		
		// Mysql backup monitoring
		String mySqlBackupFilePathDriveName = (constraints.getMySqlBackupPath().substring(0, 1));
		MysqlBackupMonitoring mysqlBackup = new MysqlBackupMonitoring(constraints.getMySqlBackupPath());
		String mysqlBackupFileData = mysqlBackup.displayMysqlBackupMonitoring();
		String mySqlBackUpFileSizewithUnit = mysqlBackup.mySqlBackupFileSize;
		double mySqlBackUpFileSizeInMB= -1;
		String mysqlBackupFileDataError = null;
		double DiskFreeSpaceInMB = -1;
		
		DiskSpaceAnalyzer diskAnalyzer = new  DiskSpaceAnalyzer();
		List<DiskSpaceAnalyzer> diskSpaceAnalyzerList = diskAnalyzer.getDiskInfo(null);
		for(DiskSpaceAnalyzer diskSpaceObj : diskSpaceAnalyzerList){
			if(diskSpaceObj.validatedEmailTrigger(constraints.getDiskUsedSpacePercentage())){
				triggerEmailNotification = true;
			}
			if(mySqlBackUpFileSizewithUnit != null  && !mySqlBackUpFileSizewithUnit.isEmpty() && diskSpaceObj.DriveName.replace(":\\", "").equals(mySqlBackupFilePathDriveName)) {
				if((mySqlBackUpFileSizewithUnit.toLowerCase().contains("gb"))) {
					mySqlBackUpFileSizeInMB = new Double(mySqlBackUpFileSizewithUnit.toLowerCase().replace("gb", "").trim()) * 1024;
				}
				else {
					mySqlBackUpFileSizeInMB = new Double(mySqlBackUpFileSizewithUnit.toLowerCase().replace("mb", "").trim());
				}
				if(diskSpaceObj.unit.equalsIgnoreCase("gb")) {
					DiskFreeSpaceInMB = (diskSpaceObj.freeSpace)*1024;
				}
				else {
					DiskFreeSpaceInMB = diskSpaceObj.freeSpace;
				}
				if((DiskFreeSpaceInMB) < (mySqlBackUpFileSizeInMB * 2)) {
					triggerEmailNotification = true;
					mysqlBackupFileDataError = mysqlBackupFileData.replace("color:#111111", "color:#ff0000");
				}
			}
		}
		
			System.out.println("Checking for Services");
			LoggerUtils.appendLog("Checking for Services");
		

		// Checking for Services Running on server.
		ServiceMonitoring serviceMonitoring = new ServiceMonitoring(constraints.getServicesNameToMonitor());
		Map<String, String> serviceNamesStatusMap = serviceMonitoring.serviceMonitoringAnalyzer();
		if(!triggerEmailNotification){
			triggerEmailNotification = serviceMonitoring.validatedEmailTrigger(serviceNamesStatusMap, "STOPPED");
		}

		System.out.println("Checking for Tomcat Log");
		LoggerUtils.appendLog("Checking for Tomcat Log");
		// Checking for Tomcat Log size

		String cssStyleTomcat8 = "";
		CheckTomactLogs ceckTomcatLog = new CheckTomactLogs();
		double Tomcat8folderSize = -1;
		if(constraints.getServicesNameToMonitor() != null && constraints.getServicesNameToMonitor().contains("Tomcat8")){
			Tomcat8folderSize = ceckTomcatLog.getTomcatLogsSize("Tomcat8");
			triggerPoint = Double.parseDouble(constraints.getTomcatLogsFolderSizeMB()); 				
			if(triggerPoint <= Tomcat8folderSize){
				if(!triggerEmailNotification){
					triggerEmailNotification = true;
				}
				cssStyleTomcat8 = "color:#ff0000";
			}
		}

		String cssStyleTomcat7 = "";
		double Tomcat7folderSize = -1;
		if(constraints.getServicesNameToMonitor() != null && constraints.getServicesNameToMonitor().contains("Tomcat7")){
			Tomcat7folderSize = ceckTomcatLog.getTomcatLogsSize("Tomcat7");
			triggerPoint = Double.parseDouble(constraints.getTomcatLogsFolderSizeMB()); 
			if(triggerPoint <= Tomcat7folderSize){
				if(!triggerEmailNotification){
					triggerEmailNotification = true;
				}
				cssStyleTomcat7 = "color:#ff0000";
			}
		}

		System.out.println("Checking for RAM");
		LoggerUtils.appendLog("Checking for RAM");
		// Checking for RAM usage
		String cssStyleRAM = "";
		RamUsage ru = new RamUsage(constraints.getRamMonitoringTimeInSeconds());

		System.out.println("Checking for CPU");
		LoggerUtils.appendLog("Checking for CPU");
		// Checking for CUP Utilization Log size
		CpuUtilizationAnalyzer cpu = new CpuUtilizationAnalyzer(constraints.getCpuMonitoringTimeInSeconds());
		cpu.start();
		ru.start();
		do{
			//Sleep main thread till we get all values for email
			Thread.sleep(1000);
		}while(cpu.continueLoop || ru.continueLoop);

		String cssStyleCPU = "";

		Double cputriggerPoint = Double.parseDouble(constraints.getCpuUsagePercentage()); 
		if(cputriggerPoint <= cpu.totalCpuAverage){
			if(!triggerEmailNotification){
				triggerEmailNotification = true;
			}
			cssStyleCPU = "color:#ff0000";
		}
		
		triggerPoint = Double.parseDouble(constraints.getRamUsagePercentage()); 
		if(triggerPoint <= ru.ramUsagePercentageAverage){
			if(!triggerEmailNotification){
				triggerEmailNotification = true;
			}
			cssStyleRAM = "color:#ff0000"; 
		}

		if(cpu.cpuDeviceIdLoadPercentageAverageMap !=  null && !cpu.cpuDeviceIdLoadPercentageAverageMap.isEmpty()){
			for(Map.Entry<String, Double> cpuDeviceIdLoadPercentage : cpu.cpuDeviceIdLoadPercentageAverageMap.entrySet()) {
				if(cputriggerPoint <= cpuDeviceIdLoadPercentage.getValue()){
					if(!triggerEmailNotification){
						triggerEmailNotification = true;
					}
				}
			}
		}

		
		
		System.out.println("Checking finished");
		LoggerUtils.appendLog("Checking finished");


		String clientName = constraints.getClientName();
		List<String> 	emailsTo =  constraints.getEmailsTo();
		String emailSubject = "Server Monitoring Issue on "+ clientName;

		// If email is already triggered for an issue. Next gentle reminder email should be sent after specified interval
		if(lastErrorEmailTriggeredOn > 0){
			if(triggerEmailNotification){
				emailSubject = "Gentle reminder : "+ emailSubject;
				if(constraints.getRemainderEmailTriggerInterval() != null && !constraints.getRemainderEmailTriggerInterval().isEmpty()) {
					long remainderEmailTriggerInterval = Integer.valueOf(constraints.getRemainderEmailTriggerInterval()) * 60000;
					long timeAfterLastEmailSent = Calendar.getInstance().getTimeInMillis() - lastErrorEmailTriggeredOn;
					if(timeAfterLastEmailSent <= remainderEmailTriggerInterval) {
						triggerEmailNotification = false;
					}
				}
			}
			else{
				// If email is already triggered for an issue and if that is resolved then it should notify by email
				emailSubject = "Resolved : Server Monitoring Issue on "+ clientName;
				lastErrorEmailTriggeredOn = -1L;
			}
		}

		// If called by reporting scheduler then email should be trigger
		if(calledByReportingScheduler){
			triggerEmailNotification = true;
			emailsTo.clear();
			emailsTo.addAll(constraints.getScheduledReportEmailTo());
			emailSubject = "Server Monitoring Report";
		}

		System.out.println(triggerEmailNotification + "Email will be sent to "+ emailsTo);
		if(triggerEmailNotification){

			System.out.println("Preparing for email...");
			LoggerUtils.appendLog("Preparing for email...");

			//Email contents
			EmailUtils eu = new EmailUtils();
			StringBuilder emailMessage = new StringBuilder();
			emailMessage.append("<table style=\"border: 1px solid black; border-collapse:inherit;\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">");
			emailMessage.append("	<tr>");
			emailMessage.append("		<td colspan=\"2\" align=\"center\" style=\"border-bottom: 1px solid black;\"><b><font size=\"4\">Site Name : </font> </b>"+ clientName +"</td>");
			emailMessage.append("	</tr>");
			emailMessage.append("	<tr>");
			emailMessage.append("		<td colspan=\"2\" align=\"center\" style=\"border-bottom: 1px solid black;\"><b><font size=\"4\">Server : </font> </b>"+ constraints.getServerName() +"</td>");
			emailMessage.append("	</tr>");


			// Disk Monitoring Details
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" ><b><font size=\"4\">Disk Monitoring Details</font> </b></td>");
			emailMessage.append("	</tr>");
			emailMessage.append(diskAnalyzer.displayObjectInHTML(constraints.getDiskUsedSpacePercentage(), diskSpaceAnalyzerList));

			// Average CPU Usage
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" style=\"border: 1px solid black;\" ><b><font size=\"4\">CPU Utilization Details</font> </b></td>");
			emailMessage.append("	</tr>");
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" style=\"border: 1px solid black;"+ cssStyleCPU +"\" ><b><font size=\"4\">Total Average CPU Usage : </font></b>"+ cpu.toString() +" % </td>");
			emailMessage.append("	</tr>");
			emailMessage.append(cpu.displayCpuUtilizationInHTML(cputriggerPoint));

			// RAM Usage
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" style=\"border: 1px solid black;\"><b><font size=\"4\">RAM Usage Details</font> </b></td>");
			emailMessage.append("	</tr>");
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" style=\"border: 1px solid black;\"><b><font size=\"3\">Total RAM : </font></b>"+ ru.totalPhysicalMemory +" MB</td>");
			emailMessage.append("	</tr>");
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" style=\"border: 1px solid black;"+ cssStyleRAM +"\"><b><font size=\"3\">RAM Usage : </font></b>"+ ru.ramUsagePercentageAverage +" %</td>");
			emailMessage.append("	</tr>");

			// Service monitoring
			emailMessage.append(serviceMonitoring.displayServicesStatusInHTML(serviceNamesStatusMap));

			// Tomcat log folder size
			if(Tomcat8folderSize >= 0){
				emailMessage.append("	<tr>");
				emailMessage.append("		<td align=\"center\" style=\""+cssStyleTomcat8+"\"><b><font size=\"4\">Tomcat 8 Log Folder Size :</font></b>"+ Tomcat8folderSize +" MB</td>");
				emailMessage.append("	</tr>");
			}
			if(Tomcat7folderSize >= 0){
				emailMessage.append("	<tr>");
				emailMessage.append("		<td align=\"center\" style=\""+cssStyleTomcat7+"\"><b><font size=\"4\">Tomcat 7 Log Folder Size :</font></b>"+ Tomcat7folderSize +" MB</td>");
				emailMessage.append("	</tr>");
			}

			// Mysql Backup monitoring
			if(constraints.getMySqlBackupPath() != null && !constraints.getMySqlBackupPath().equals("0")){
				if(mysqlBackupFileDataError!= null && !mysqlBackupFileDataError.isEmpty()) {
					emailMessage.append(mysqlBackupFileDataError);
				}
				else {
					emailMessage.append(mysqlBackupFileData);
				}
			}

			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"right\" style=\"padding-top: 15px;\"><font size=\"2\">RioMed Server Monitoring Tool 10.0v</font></td>");
			emailMessage.append("	</tr>");

			emailMessage.append("</table>");
			
			//	StringBuilder emailtext = eu.createAnEmailTemplate(emailMessage.toString());
			int i = 0;

			for(String sendEmailTo : emailsTo){

				/*	If you face trubble in sending email with 534-5.7.14 authenticationFailed error then 
				 *	https://myaccount.google.com/security?utm_source=OGB&utm_medium=act#connectedapps
				 *	Loging with senders account and allow policy.
				 */

				boolean isSent = eu.sentEmailForserverMonitoring(constraints.getEmailHost(), 
						constraints.getEmailPort(), 
						constraints.getEmailFromUsername(),
						constraints.getEmailFromPassword(),
						constraints.getEmailToHostType(), 
						sendEmailTo, 
						emailSubject,
						emailMessage.toString());

				if(isSent){
					if(!calledByReportingScheduler) {
						lastErrorEmailTriggeredOn = Calendar.getInstance().getTimeInMillis();
					}
					System.out.println("Email sent to "+sendEmailTo+" ...");
					LoggerUtils.appendLog("Email sent to "+sendEmailTo+" ...");
				}
				else{
					System.out.println("Email failed while sending email to "+sendEmailTo+" ...");
					LoggerUtils.appendLog("Email failed while sending email to "+sendEmailTo+" ...");
				}
				i++;
				System.out.println("Done "+ i +" out of "+ emailsTo.size() +" ..."); 
				LoggerUtils.appendLog("Done "+ i +" out of "+ emailsTo.size() +" ...");
			}
		}
		System.out.println("Program executed sucessfully");
		LoggerUtils.appendLog("Program executed sucessfully");
	}
}
