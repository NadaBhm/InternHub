package com.sip.internhub.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sip.internhub.entities.User;

@Service
public class RegistrationNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationNotificationService.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final String adminEmail;
	private final String mailHost;

	public RegistrationNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
			@Value("${app.mail.admin-email:}") String adminEmail,
			@Value("${spring.mail.host:}") String mailHost) {
		this.mailSenderProvider = mailSenderProvider;
		this.adminEmail = adminEmail;
		this.mailHost = mailHost;
	}

	public void notifyNewUser(User user) {
		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailHost.isBlank() || adminEmail.isBlank() || mailSender == null) {
			logger.warn("Registration email was not sent: configure MAIL_HOST and INTERNHUB_ADMIN_EMAIL");
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(adminEmail);
		message.setSubject("Nouvelle inscription sur InternHub");
		message.setText("Un nouvel étudiant s'est inscrit : " + user.getEmail());
		try {
			mailSender.send(message);
		} catch (MailException exception) {
			logger.error("Unable to send the registration notification for {}", user.getEmail(), exception);
		}
	}
}
