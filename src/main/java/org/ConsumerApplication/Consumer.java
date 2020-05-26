package org.ConsumerApplication;

import com.rabbitmq.client.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rundeck.api.OptionsBuilder;
import org.rundeck.api.RunJobBuilder;
import org.rundeck.api.RundeckClient;
import org.rundeck.api.domain.RundeckExecution;
import org.rundeck.api.domain.RundeckJob;
// import sun.awt.X11.XSystemTrayPeer;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class Consumer
{
    private static final long serialVersionUID = 1L;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private static void triggerMyJob(String appUrl, String appName) {
        System.out.println("triggerd");
        RundeckClient rundeck = RundeckClient.builder().url("http://206.189.134.237:4440/").login("admin", "admin1234").build();
        RundeckJob job = rundeck.getJob("debf006b-4bc5-4934-bdde-9b26eec54145");
        RundeckExecution exec = rundeck.runJob(RunJobBuilder.builder()
                .setOptions(new OptionsBuilder().addOption("APPURL", appUrl).addOption("APPNAME", appName).toProperties())
                .setJobId(job.getId())
                .build(), 10L, TimeUnit.SECONDS);
    }


    public static void sendMail(String receiver,String text) throws MessagingException {
        Properties properties=new Properties();
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.host","smtp.gmail.com");
        properties.put("mail.smtp.port","587");
        String senderEmail="apkgenwebsite@gmail.com";
        String senderEmailPassword="awesomeapk";
        Session session=Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail,senderEmailPassword);
            }
        });
        Message message=prepareMessage(session,senderEmail,receiver,text);
        Transport.send(message);
    }

    private static Message prepareMessage(Session session, String sender, String receiver,String text) throws MessagingException {
        Message message=new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.setRecipient(Message.RecipientType.TO,new InternetAddress(receiver));
        message.setSubject("Hi From ApkGen");
        message.setText(text);
        return message;
    }

    public static void main(String[] args) throws IOException,TimeoutException
    {
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("159.65.158.218");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection =factory.newConnection();
        Channel channel=connection.createChannel();

        DeliverCallback deliverCallback = (s, delivery) -> {
            String msg=new String(delivery.getBody(),"UTF-8");
            JSONParser parser=new JSONParser();
            Object obj= null;
            try
            {
                obj = parser.parse(msg);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            JSONObject jo=(JSONObject)obj;
            String urlLink=(String) jo.get("urlLink");
            String appName=(String) jo.get("appName");
            String email=(String) jo.get("Email");
            String[] appUrl = urlLink.replaceAll("/", "").trim().split("\\.");
            String FILE_URL = "http://142.93.210.19:8000/"+appName+appUrl[1]+".apk";
            URL url=new URL(FILE_URL);
            HttpURLConnection hr=(HttpURLConnection) url.openConnection();
            hr.setRequestMethod("HEAD");
            hr.connect();
            if(hr.getResponseCode()!=200)
            {
                triggerMyJob(urlLink,appName);
            }
            try
            {
                sendMail(email,FILE_URL);
            }
            catch (MessagingException e)
            {
                e.printStackTrace();
            }
        };

        channel.queueDeclare("apkGenQueue",false,false,false,null);
        channel.basicConsume("apkGenQueue", true, deliverCallback, consumerTag -> { });
    }
}

