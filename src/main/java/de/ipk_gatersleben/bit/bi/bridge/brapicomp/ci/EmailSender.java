package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;

class EmailSender {
    private static final Logger LOGGER = LogManager.getLogger(EmailSender.class.getName());

    static void sendEmail(final String message, final String subject, final String emailAddress) {

        javax.mail.Session session;

        InternetAddress addressFrom = null;

        final Properties properties = new Properties();

        properties.put("mail.smtp.host", Config.get("mailSmtpHost"));
        
        if (Config.get("mailSmtpPort") != null) {
        	properties.put("mail.smtp.port", Config.get("mailSmtpPort"));
        }

        if (Config.get("mailSmtpLogin") == null
                || Config.get("mailSmtpLogin").isEmpty()) {

            session = javax.mail.Session.getDefaultInstance(properties);

            try {
                addressFrom = new InternetAddress(Config.get("fromEmailAddress"),
                        Config.get("fromPersonalName"));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();

                LOGGER.warn(emailAddress + " : " + e.getMessage());
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
                addressFrom = new InternetAddress(Config.get("fromEmailAddress"),
                        Config.get("fromPersonalName"));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
                LOGGER.warn(emailAddress + " : " + e.getMessage());
            }

        }

        try {
            final Message mail = new MimeMessage(session);

            final InternetAddress addressTo = new InternetAddress(emailAddress);
            mail.setRecipient(Message.RecipientType.TO, addressTo);
            mail.setSubject(subject);
            mail.setFrom(addressFrom);

            Multipart multipart = new MimeMultipart();

            MimeBodyPart content = new MimeBodyPart();
            content.setText(message, "UTF-8", "html");

            multipart.addBodyPart(content);


            mail.setContent(multipart);
            Transport.send(mail);
            //System.out.println(message);

        } catch (final MessagingException e) {
            e.printStackTrace();

            LOGGER.warn(emailAddress + " : " + e.getMessage());
        }
    }
}
