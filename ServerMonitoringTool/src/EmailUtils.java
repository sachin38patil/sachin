import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//send email
public class EmailUtils {
	public boolean sentEmailForserverMonitoring (String emailHost, String emailPort ,String emailUsername , String emailPassword, String emailHostType, String emailTo, String emailSubject , String emailMessage) throws AddressException, MessagingException, IOException{
		Properties props = new Properties();
		if(emailHost == null || emailHost.length() < 1 
				|| emailPort == null || emailPort.length() < 1 
				|| emailUsername == null || emailUsername.length() < 1) {
			return false;
		}
		
		if(emailHostType == null || !emailHostType.equals("exchange")){
			if(emailPassword == null || emailPassword.isEmpty()){
				return false;
			}
		}
		
		props.put("mail.smtp.host", emailHost);
    	props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", emailPort);	
		
		if(emailPort != null && emailPort.equals("587")){
			props.put("mail.smtp.starttls.enable", "true"); 
		}

		try {
			Session sessions = null;
			Message message = null;
			if(emailHostType != null && emailHostType.equals("exchange") && (emailPassword == null || emailPassword.isEmpty())) {
					props.put("mail.smtp.auth", "false");
					sessions = Session.getDefaultInstance(props);
			}
			else {
				props.put("mail.smtp.socketFactory.port", emailPort);
				props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
				sessions = Session.getInstance(props, new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(emailUsername, emailPassword);
					}
				});
			}
			message = new MimeMessage(sessions);
			message.setFrom(new InternetAddress(emailUsername));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(emailTo));
			message.setSubject(emailSubject);

			// This mail has 2 part, the BODY and the embedded image
			MimeMultipart multipart = new MimeMultipart();

			// first part (the html)
			BodyPart messageBodyPart = new MimeBodyPart();
			//   messageBodyPart.setText(emailMessage);
			messageBodyPart.setContent(emailMessage, "text/html; charset=utf-8");
			// add it
			multipart.addBodyPart(messageBodyPart);

			// put everything together
			message.setContent(multipart);
			// Send message
			Transport.send(message);
		}
		catch (SendFailedException e) {
			e.printStackTrace();
			
			StringWriter stackTraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceWriter, true);
        	e.printStackTrace(printWriter);
			LoggerUtils.appendLog(stackTraceWriter.toString());
			return false;
		}
		catch (MessagingException e) {
			e.printStackTrace();
			StringWriter stackTraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceWriter, true);
        	e.printStackTrace(printWriter);
			LoggerUtils.appendLog(stackTraceWriter.toString());
			return false;
		}
		catch(Exception e){
			e.printStackTrace();
			StringWriter stackTraceWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stackTraceWriter, true);
        	e.printStackTrace(printWriter);
			LoggerUtils.appendLog(stackTraceWriter.toString());
			return false;
		}
		return true;
	}
}