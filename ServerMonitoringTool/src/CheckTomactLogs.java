import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class will be used to get check tomcat log file size.
 * @author Ragini Mahajan
 * @Date 14th Nov 2018
 */
public class CheckTomactLogs {
	
	/**
	 * Get Tomcat Log file folder size
	 * @return log folder size
	 */
	public double getTomcatLogsSize(String servicesName) {
		double folderSize = -1.0;
		try {
			Runtime rt = Runtime.getRuntime();
			String readLine = "";
			Process proc = rt.exec("sc qc \""+servicesName+"\"");
			StringBuffer line = new StringBuffer();
			BufferedReader input =  new BufferedReader (new InputStreamReader(proc.getInputStream()));
			while((readLine = input.readLine()) != null) {
				if(readLine.contains("BINARY_PATH_NAME")){
					line.append(readLine);	
					break;
				}
			}
			
			readLine = line.toString().trim();
			if(readLine.length() > 0){
				readLine = readLine.substring(readLine.indexOf(":")).trim();
				if(readLine.indexOf("\"") != -1){
					readLine = readLine.substring(readLine.indexOf("\"") + 1, readLine.lastIndexOf("\"")).trim();
					readLine = readLine.substring(0, readLine.lastIndexOf("\\")).trim();
					readLine = readLine.substring(0, readLine.lastIndexOf("\\")).trim();
					readLine = readLine + "\\logs";
					readLine = readLine.replace("\\","\\\\");
					File directory = new File(readLine);
					double length = 0;
					if(directory != null && directory.exists() && directory.listFiles() != null){
						System.out.println("Tomcat log directory Path : "+directory.getAbsolutePath());
						System.out.println("Tomcat log directory files count : "+directory.listFiles().length);
						for (File file : directory.listFiles()) {
							if (file.isFile())
							{
								length += file.length();
							}
						}
					}
					
					if(length > 0){
						double logsSizeInMB = length / (1024*1024);
						BigDecimal bd = new BigDecimal(logsSizeInMB).setScale(2, RoundingMode.HALF_EVEN);
						folderSize = bd.doubleValue();
					}
				}
			}
			System.out.println("Tomcat Logs checked for "+servicesName);
		}
		catch(IOException e) {
			System.out.println(e);
		}
		return folderSize;
	}
	
}