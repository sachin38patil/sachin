import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class will be used to get system information server.
 * @author Ragini Mahajan
 * @Date 14th Nov 2018
 */
public class SystemInfo {
	
	/**
	 * Get System information and display in HTML format
	 * @return System formation in sting format
	 * @throws IOException
	 */
	public String getDetailedSystemInfo(){
		StringBuffer line = new StringBuffer();
		try{
			String readLine = "";
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("systeminfo");
			BufferedReader input =  new BufferedReader (new InputStreamReader(proc.getInputStream()));  
			while ((readLine = input.readLine()) != null) {
				if(readLine.contains("Hotfix(s):")) {
					break;
				}
				String lines[] = readLine.split(": ");
				if(lines.length < 2) {
					continue;
				}
				line.append("<tr><td>"+lines[0]+"</td><td>"+lines[1]+"</td></tr>");
			}	
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return line.toString();
	}
}