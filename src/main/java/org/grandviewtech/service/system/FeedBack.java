package org.grandviewtech.service.system;

import java.util.Date;
import java.util.Properties;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class FeedBack
	{
		public static void sendFeedBack(SimpleMailMessage simpleMailMessage)
			{
				MailSender mailSender = getMailSender();
				mailSender.send(simpleMailMessage);
				
			}
			
		private static MailSender getMailSender()
			{
				JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
				javaMailSender.setHost(PropertyReader.getProperties("host"));
				javaMailSender.setPort(Integer.parseInt(PropertyReader.getProperties("port")));
				javaMailSender.setJavaMailProperties(getMailProperties());
				return javaMailSender;
			}
			
		private static Properties getMailProperties()
			{
				Properties properties = new Properties();
				properties.setProperty("mail.transport.protocol", "smtp");
				properties.setProperty("mail.smtp.auth", "false");
				properties.setProperty("mail.smtp.starttls.enable", "false");
				properties.setProperty("mail.debug", "false");
				return properties;
			}
			
		public static void main(String[] args)
			{
				SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
				simpleMailMessage.setSentDate(new Date());
				sendFeedBack(simpleMailMessage);
			}
	}
