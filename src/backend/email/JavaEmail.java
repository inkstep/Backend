package email;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.sun.mail.pop3.POP3Store;

public class JavaEmail {

  private String smtpPort = "587";
  private String popPort = "995";
  private Properties emailSmtpProp;
  private Properties emailPopProp;
  private Session mailSession;
  private MimeMessage emailMessage;
  private String emailStmpHost = System.getenv("INKSTEP_EMAIL_HOST");
  private String emailAccount = System.getenv("INKSTEP_EMAIL_ACCOUNT");
  private String emailPassword = System.getenv("INKSTEP_EMAIL_PASSWORD");
  private String emailAddress = System.getenv("INKSTEP_EMAIL_ADDRESS");

  private void setMailServerProperties() {
    emailSmtpProp = System.getProperties();
    emailSmtpProp.put("mail.smtp.port", smtpPort);
    emailSmtpProp.put("mail.smtp.auth", "true");
    emailSmtpProp.put("mail.smtp.starttls.enable", "true");

    emailPopProp = System.getProperties();
    emailPopProp.put("mail.pop3s.host", "pop.gmail.com");
    emailPopProp.put("mail.pop3s.port", popPort);
    emailPopProp.put("mail.pop3s.auth", "true");
    emailPopProp.put("mail.pop3s.starttls.enable", "true");
  }

  private void createEmailMessage(String to, String message, String subject, String toReply,
    List<File> files, boolean html) throws MessagingException {
    String[] toEmails = {to};

    mailSession = Session.getDefaultInstance(emailSmtpProp, null);
    emailMessage = new MimeMessage(mailSession);

    for (String toEmail : toEmails) {
      emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
    }

    Address[] replyAddresses = {new InternetAddress(toReply)};
    emailMessage.setReplyTo(replyAddresses);
    emailMessage.setSubject(subject);

    // Set the body
    BodyPart messageBodyPart = new MimeBodyPart();
    if (html) {
      messageBodyPart.setContent(message, "text/html");
    } else {
      messageBodyPart.setText(message);
    }

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);

    int imgCount = 0;
    for (File file : files) {
      // Add attachment
      messageBodyPart = new MimeBodyPart();
      DataSource source = new FileDataSource(file);
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName("refImg" + imgCount + ".png");
      multipart.addBodyPart(messageBodyPart);
      imgCount++;
    }

    // Send the complete message parts
    emailMessage.setContent(multipart);
  }

  public void sendEmail(String to, String message, String subject, String toReply,
    List<File> files, boolean html) throws MessagingException {
    setMailServerProperties();
    createEmailMessage(to, message, subject, toReply, files, html);

    Transport transport = mailSession.getTransport("smtp");
    System.out.println(String.format("Connecting on transport smtp: %s %s %s", emailStmpHost, emailAccount, emailPassword.length()));
    transport.connect(emailStmpHost, emailAccount, emailPassword);
    transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
    transport.close();
    System.out.println("email sent successfully.");
  }

  public JavaMessage[] receiveEmail() {
    try {
      setMailServerProperties();
      Session emailSession = Session.getDefaultInstance(emailPopProp);
      POP3Store emailStore = (POP3Store) emailSession.getStore("pop3s");
      emailStore.connect(emailAddress, emailPassword);

      Folder emailFolder = emailStore.getFolder("INBOX");
      emailFolder.open(Folder.READ_WRITE);

      //4) retrieve the messages from the folder in an array and print it
      Message[] messages = emailFolder.getMessages();
      JavaMessage[] javaMessages = new JavaMessage[messages.length];
      for (int i = 0; i < messages.length; i++) {
        Message message = messages[i];
        javaMessages[i] = new JavaMessage(
            message.getSubject(),
            message.getFrom()[0].toString(),
            getTextFromMessage(message)
        );
        message.setFlag(Flag.DELETED, true);
      }

      //5) close the store and folder objects
      emailFolder.close(true);
      emailStore.close();

      return javaMessages;
    } catch (Exception e) {
      System.out.println(e);
    }

    return new JavaMessage[0];
  }

  private String getTextFromMessage(Message message) {
    String result = "";
    try {
      if (message.isMimeType("text/plain")) {
        result = message.getContent().toString();
      } else if (message.isMimeType("multipart/*")) {
        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
        result = getTextFromMimeMultipart(mimeMultipart);
      }
    } catch (MessagingException | IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  private String getTextFromMimeMultipart(
      MimeMultipart mimeMultipart) throws IOException, MessagingException {

    int count = mimeMultipart.getCount();
    if (count == 0) {
      throw new MessagingException("Multipart with no body parts not supported.");
    }

    boolean multipartAlt = new ContentType(
        mimeMultipart.getContentType()
    ).match("multipart/alternative");

    if (multipartAlt) {
      // alternatives appear in an order of increasing
      // faithfulness to the original content. Customize as req'd.
      return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
    }

    String result = "";
    for (int i = 0; i < count; i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      result += getTextFromBodyPart(bodyPart);
    }
    return result;
  }

  private String getTextFromBodyPart(
      BodyPart bodyPart) throws IOException, MessagingException {
    String result = "";
    if (bodyPart.isMimeType("text/plain")) {
      result = (String) bodyPart.getContent();
    } else if (bodyPart.isMimeType("text/html")) {
      String html = (String) bodyPart.getContent();
      result = org.jsoup.Jsoup.parse(html).text();
    } else if (bodyPart.getContent() instanceof MimeMultipart) {
      result = getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
    }
    return result;
  }
}
