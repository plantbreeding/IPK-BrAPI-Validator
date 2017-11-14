package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;

public class EmailSender {
	private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());
	protected static void sendEmail(final String message, final String subject, final String emailAddress) {

		javax.mail.Session session = null;

		InternetAddress addressFrom = null;

		final Properties properties = new Properties();

		properties.put("mail.smtp.host", Config.get("mailSmtpHost"));

		if (Config.get("mailSmtpLogin") == null
				|| Config.get("mailSmtpLogin").isEmpty()) {

			session = javax.mail.Session.getDefaultInstance(properties);

			try {
				addressFrom = new InternetAddress(Config.get("fromEmailAddress"),
						Config.get("fromPersonalName"));
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();

				LOGGER.severe(emailAddress + " : " + e.getMessage());
			}

		} else {

			properties.put("mail.smtp.auth", "true");

			final Authenticator authenticator = new Authenticator() {
				private PasswordAuthentication authentication;

				{
					this.authentication = new PasswordAuthentication(Config.get("mailSmtpLogin"),
							Config.get("mailSmtpPassword"));
				}

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return this.authentication;
				}
			};

			session = javax.mail.Session.getInstance(properties, authenticator);

			try {
				addressFrom = new InternetAddress(Config.get("mailSmtpHost"),
						Config.get("fromPersonalName"));
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
				LOGGER.severe(emailAddress + " : " + e.getMessage());
			}

		}

		final Message mail = new MimeMessage(session);
		try {

			mail.setFrom(addressFrom);
			final InternetAddress addressTo = new InternetAddress(emailAddress);
			mail.setRecipient(Message.RecipientType.TO, addressTo);
			mail.setSubject(subject);
			mail.setContent(message, "text/html; charset=UTF-8");

			Transport.send(mail);

		} catch (final MessagingException e) {
			e.printStackTrace();

			LOGGER.severe(emailAddress + " : " + e.getMessage());
		}
	}
}
