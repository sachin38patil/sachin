import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * This Utils contains logger methods.
 * @author Onkar Paranjape
 */
public class LoggerUtils {
	/**
	 * Writing logs in log file
	 * @param message
	 * @throws IOException
	 */
	public static void appendLog(String message){
		FileOutputStream fileOutputStream = null;
		try {
			File logFile = new File("logs.txt");
			long fileSizeInMB = 0;
			if(logFile.length() > 0){
				fileSizeInMB = logFile.length() / (1024*1024);
			}
			if(logFile.exists() &&  fileSizeInMB < (5 * 1024*1024)){
				fileOutputStream = new FileOutputStream(logFile, true);
			}
			else{
				fileOutputStream = new FileOutputStream(logFile, false);
			}

			Calendar c = Calendar.getInstance();
			Timestamp ts = new Timestamp(c.getTimeInMillis());     
			message = "\n " + ts.toString() + "\t" + message;

			fileOutputStream.write(message.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(fileOutputStream != null){
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
