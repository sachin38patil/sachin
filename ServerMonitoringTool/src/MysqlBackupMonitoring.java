
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
public class MysqlBackupMonitoring {
	
	String MysqlBackupDirectoryPath = null;
	String mySqlBackupFileSize = null;
	
	public MysqlBackupMonitoring(String MysqlBackupDirectoryPath) {
		this.MysqlBackupDirectoryPath = MysqlBackupDirectoryPath;
	}
	public String displayMysqlBackupMonitoring() {
		
		StringBuilder emailMessage = new StringBuilder("");
		//if directory path is not set we will not monitor MySQL backup
		if(MysqlBackupDirectoryPath == null || MysqlBackupDirectoryPath.isEmpty()) {
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" style=\"font-size:30px; \"color:#ff0000\"><b><font size=\"4\">MySQL Backups Directory path not specified</font></b> </td>");
			emailMessage.append("	</tr>");
			return emailMessage.toString();
		}
		File mySqlBackupDirectory = new File(MysqlBackupDirectoryPath);
		if(mySqlBackupDirectory.exists()) {
			File filesList[] = mySqlBackupDirectory.listFiles();
			if(filesList == null || filesList.length == 0){
				emailMessage.append("	<tr>");
				emailMessage.append("		<td align=\"center\" style=\"font-size:30px; \"color:#ff0000\"><b><font size=\"4\">MySQL Backups Directory does not have backup files</font></b> </td>");
				emailMessage.append("	</tr>");
			}
			else{
				long lastModifiedMilliseconds = -1L;
				File lastModifiedFile = null;
				for(File file : filesList) {
					if(file != null && file.canRead()) {
						if(lastModifiedMilliseconds < file.lastModified()) {
							lastModifiedMilliseconds = file.lastModified();
							lastModifiedFile = file; 
						}
					}
				}
				
				if(lastModifiedFile != null){
					emailMessage.append("	<tr>");
					emailMessage.append("		<td align=\"center\"><b><font size=\"4\">MySQL Backups</font></b> </td>");
					emailMessage.append("	</tr>");
					
					emailMessage.append("	<tr>");
					emailMessage.append("		<td>");
					emailMessage.append("			<table style=\"border-spacing: 0px;border-collapse: collapse;\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
					emailMessage.append("				<tr>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black;\"><b>BackUp files path</b></td>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black;\"><b>Backup File Name</b></td>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black;\"><b>Backup Size(GB)</b></td>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black;\"><b>Last Modified Date</b></td>");
					emailMessage.append("				</tr>");
					
					long backupFileSize = lastModifiedFile.length();
					
					String backupFileSizeInUnits = "";
					if(backupFileSize != 0){
						if((backupFileSize /(1024 * 1024)) > 1000){
							backupFileSizeInUnits = (backupFileSize / (1024 * 1024 * 1024)) +" GB";
							mySqlBackupFileSize = backupFileSizeInUnits;
						}
						else{
							backupFileSizeInUnits = (backupFileSize / (1024 * 1024)) +" MB";
							mySqlBackupFileSize = backupFileSizeInUnits;
						}
						
					}
					else{
						backupFileSizeInUnits = "0 MB";
						mySqlBackupFileSize = backupFileSizeInUnits;
					}
				
					Timestamp lastModified = new Timestamp(lastModifiedMilliseconds);
					SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					emailMessage.append("				<tr>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black; color:#111111\">"+MysqlBackupDirectoryPath+"</td>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black; color:#111111\">"+lastModifiedFile.getName()+"</td>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black; color:#111111\">"+backupFileSizeInUnits+"</td>");
					emailMessage.append("					<td align=\"center\" style=\"border: 1px solid black; color:#111111\">"+sdformat.format(lastModified)+"</td>");
					emailMessage.append("				</tr>");
				}
				
					emailMessage.append("			</table>");
					emailMessage.append("		</td>");
					emailMessage.append("	</tr>");
			}
		}
		else {
			emailMessage.append("	<tr>");
			emailMessage.append("		<td align=\"center\" \"color:#ff0000\"><b><font size=\"4\">MySQL Backups Directory path is invalid</font></b> </td>");
			emailMessage.append("	</tr>");
		}
		return emailMessage.toString();
	}
	
}
