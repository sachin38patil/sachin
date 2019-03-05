import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

/**
 * The ServerMonitoringTest class check server status in regards to CPU utilization, RAM usages, Disk Space Analyzer
 * System information and Services monitoring. 
 * 
 * It will also trigger email if parameter constraints are been set.
 * @author Onkar Paranjape
 *
 */
class ScheduledServerMonitoring implements Runnable {
	
	boolean isReportingScheduler;

	public ScheduledServerMonitoring(boolean isReportingScheduler){
		this.isReportingScheduler = isReportingScheduler;
	}
	
	public void run(){
		try {
			ServerMonitoring.performServerMonitoring(isReportingScheduler);
			
		} catch (MessagingException e) {
			e.printStackTrace();
			StringWriter stackTraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceWriter, true);
        	e.printStackTrace(printWriter);
			LoggerUtils.appendLog(stackTraceWriter.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
			StringWriter stackTraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceWriter, true);
        	e.printStackTrace(printWriter);
			LoggerUtils.appendLog(stackTraceWriter.toString());
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			StringWriter stackTraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceWriter, true);
        	e.printStackTrace(printWriter);
			LoggerUtils.appendLog(stackTraceWriter.toString());
		}
	}
}


public class ServerMonitoringMain {
	
	public static void main(String args[]) throws Exception {
		
		// Reading inputs
		FileInputStream fileInputStream = new FileInputStream("ConstraintSpecification.txt"); 
		Properties propConstraints = new Properties();
		propConstraints.load(fileInputStream);

		// Create ScheduledExecutorService if 
		if(propConstraints == null || propConstraints.isEmpty()){
			System.out.println("Error while reading file...");
			return;
		}
		
		// Report Generation based on scheduler
		generateReports(propConstraints);
		
		// Schedule checking with parameters
		runScheduleCheck(propConstraints);
		
		ServerMonitoring.performServerMonitoring(true);
	}
	
	/**
	 * Server monitoring tool sent report through email. Report email will be on sent at specified time.
	 * Time and schedule will be specified in constaint file.
	 * 
	 * @param propConstraints - Constraint file in Property Object
	 * @return true if all good else false as well as it keeps a log
	 * @throws ParseException 
	 */
	static boolean generateReports(Properties propConstraints) throws ParseException{
		String scheduledReport = (String) propConstraints.get("Scheduled_Report");
		String scheduledReportIntervalHours = (String) propConstraints.get("Scheduled_Report_Interval_Hours");
		String scheduledReportIntervalDays = (String) propConstraints.get("Scheduled_Report_Interval_Days");
		if(scheduledReport != null && scheduledReport.equals("Yes")){
			long difference = 0;
			if(propConstraints.get("Scheduled_Report_Time_HH_MM_SS") != null){
				String scheduledReportTimeHHMMSS = (String) propConstraints.get("Scheduled_Report_Time_HH_MM_SS");
				DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				String currentTime = dateFormat.format(new Date());
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				Date date1 = format.parse(scheduledReportTimeHHMMSS.replace("_", ":"));
				Date date2 = format.parse(currentTime);
				difference = date1.getTime() - date2.getTime();
				if(difference < 0){
					difference = (24*60*60*1000) - (difference * -1);
				}
			}
			
			ScheduledServerMonitoring serverMonitoringReport = new ScheduledServerMonitoring(true);
			ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
			if(scheduledReportIntervalHours != null && !scheduledReportIntervalHours.isEmpty() && Integer.parseInt(scheduledReportIntervalHours) > 0){
				long scheduleTime = Long.parseLong(scheduledReportIntervalHours) * 60 * 60 * 1000;
				if(difference == 0){
					difference = scheduleTime;
				}
				service.scheduleAtFixedRate(serverMonitoringReport, difference, scheduleTime, TimeUnit.MILLISECONDS);
				return true;
			}
			else if(scheduledReportIntervalDays != null && !scheduledReportIntervalDays.isEmpty() && Integer.parseInt(scheduledReportIntervalDays) > 0){
				long scheduleTime = Long.parseLong(scheduledReportIntervalDays) * 86400000;
				if(difference == 0){
					difference = scheduleTime;
				}
				service.scheduleAtFixedRate(serverMonitoringReport, difference, scheduleTime, TimeUnit.MILLISECONDS);
				return true;
			}
		}
		System.out.println("Failed to trigger reports. Invalid input");
		LoggerUtils.appendLog("Failed to trigger reports. Invalid input");
		return false;
	}
	
	
	/**
	 * Server monitoring tool check server status after specified interval. If any lookup goes above specified range constraints
	 * it will trigger and email.
	 * 
	 * @param propConstraints - Constraint file in Property Object
	 * @return true if all good else false as well as it keeps a log
	 */
	static boolean runScheduleCheck(Properties propConstraints){
		String scheduledIntervalMinutes = (String) propConstraints.get("Scheduled_Interval_Minutes");
		String scheduledIntervalHours = (String) propConstraints.get("Scheduled_Interval_Hours");
		String scheduledIntervalDays = (String) propConstraints.get("Scheduled_Interval_Days");
		if(scheduledIntervalMinutes != null && scheduledIntervalHours != null && scheduledIntervalDays != null){
			ScheduledServerMonitoring serverMonitoring = new ScheduledServerMonitoring(false);
			ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
			if(scheduledIntervalMinutes != null && !scheduledIntervalMinutes.isEmpty() && Integer.parseInt(scheduledIntervalMinutes) > 0){
				long scheduleTime = Long.parseLong(scheduledIntervalMinutes);
				service.scheduleAtFixedRate(serverMonitoring, 0, scheduleTime, TimeUnit.MINUTES);
				return true;
			}
			else if(scheduledIntervalHours != null && !scheduledIntervalHours.isEmpty() && Integer.parseInt(scheduledIntervalHours) > 0){
				long scheduleTime = Long.parseLong(scheduledIntervalHours);
				service.scheduleAtFixedRate(serverMonitoring, 0, scheduleTime, TimeUnit.HOURS);
				return true;
			}
			else if(scheduledIntervalDays != null && !scheduledIntervalDays.isEmpty() && Integer.parseInt(scheduledIntervalDays) > 0){
				long scheduleTime = Long.parseLong(scheduledIntervalDays);
				service.scheduleAtFixedRate(serverMonitoring, 0, scheduleTime, TimeUnit.DAYS);
				return true;
			}
		}
		System.out.println("Failed to trigger schedule check. Invalid input");
		LoggerUtils.appendLog("Failed to trigger schedule check. Invalid input");
		return false;
	}

}