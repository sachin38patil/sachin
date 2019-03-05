import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;


/**
 * This class will be used to monitor hard disc drive space and network drives.
 * @author Onkar Paranjape
 * @Date 14th Nov 2018
 */
public class DiskSpaceAnalyzer {
	
	double totalSpace;
	double freeSpace;
	double usedSpaceInPercentage;
	String type;
	String DriveName;
	String unit;

	public boolean validatedEmailTrigger(String triggerPointConstaint){
		if(triggerPointConstaint != null){
			double triggerPoint = Double.parseDouble(triggerPointConstaint); 
			if(triggerPoint <= usedSpaceInPercentage){
				return true;
			}
		}
		return false;
	}
	
	/** Represent object in String format to integrate in email */
	public String displayObjectInHTML(String triggerPointConstaint, List<DiskSpaceAnalyzer> diskSpaceAnalyzerList) {
		String cssStyle = "style=\"border: 1px solid black;\"";
		if(triggerPointConstaint != null){
			double triggerPoint = Double.parseDouble(triggerPointConstaint); 
			if(triggerPoint <= usedSpaceInPercentage){
				cssStyle = "style=\"color:#ff0000; border: 1px solid black;\"";
			}
		}
		
		StringBuilder emailMessage = new StringBuilder();
		if(diskSpaceAnalyzerList != null && !diskSpaceAnalyzerList.isEmpty()){
			emailMessage.append("<tr>");
			emailMessage.append("	<td>");
			emailMessage.append("		<table style=\"border-spacing: 0px;border-collapse: collapse; border:1px solid black\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
			emailMessage.append("			<tr style=\"border-bottom: 1px solid black;\">");
			emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\"><b>Drive Name</b></td>");
			emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\"><b>Type</b></td>");
			emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\"><b>Total Space</b></td>");
			emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\"><b>Free Space</b></td>");
			emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\"><b>Used Space (%) </b></td>");
			emailMessage.append("			</tr>");
			
			for(DiskSpaceAnalyzer diskSpaceObj : diskSpaceAnalyzerList){
				emailMessage.append("			<tr>");
				emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\">"+ diskSpaceObj.DriveName +"</td>");
				emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\">"+ diskSpaceObj.type +"</td>");
				emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\">"+ String.format( "%.2f", diskSpaceObj.totalSpace) +" "+ diskSpaceObj.unit +"</td>");
				emailMessage.append("				<td align=\"center\" style=\"border: 1px solid black;\">"+ String.format( "%.2f",diskSpaceObj.freeSpace) +" "+ diskSpaceObj.unit +"</td>");
				emailMessage.append("				<td align=\"center\" "+cssStyle+">"+ diskSpaceObj.usedSpaceInPercentage +"% </td>");
				emailMessage.append("			</tr>");
			}
			emailMessage.append("		</table>"); 
			emailMessage.append("	</td>");
			emailMessage.append("</tr>");
		}
		return emailMessage.toString();
	}

	/** Check disk drives and calculate its used, free and total space.
	 *	@return DisplaySpaceAnalyzer object List
	 */
	public List<DiskSpaceAnalyzer> getDiskInfo(String unit){
		List<DiskSpaceAnalyzer> diskSpaceAnalyzerList = new ArrayList<DiskSpaceAnalyzer>();
		
		double divisor =  1024*1024*1024;// GB
		if(unit != null && unit.equalsIgnoreCase("mb")){
			divisor = 1024*1024; // MB
		}
		
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File[] drives = File.listRoots();
		if (drives != null && drives.length > 0) {
			for (File diskDrive : drives) {
				DiskSpaceAnalyzer dsa = new DiskSpaceAnalyzer();
				
				if(unit != null && unit.equalsIgnoreCase("mb")){
					dsa.unit = "MB";
				}else{
					dsa.unit = "GB";
				}
				dsa.type = fsv.getSystemTypeDescription(diskDrive);
				dsa.DriveName = diskDrive.toString();
				dsa.totalSpace = diskDrive.getTotalSpace() / divisor;
				dsa.freeSpace = diskDrive.getFreeSpace() / divisor;
				double usedSpace = dsa.totalSpace - dsa.freeSpace;
				if(dsa.type != null && dsa.type.contains("Local Disk")) {
					if(usedSpace > 0 && dsa.totalSpace > 0) {
						BigDecimal bd = new BigDecimal((usedSpace / dsa.totalSpace) * 100).setScale(3, RoundingMode.HALF_EVEN);
						dsa.usedSpaceInPercentage = bd.doubleValue();
						diskSpaceAnalyzerList.add(dsa);
					}
				}
			}
		}
		return diskSpaceAnalyzerList;
	}
}