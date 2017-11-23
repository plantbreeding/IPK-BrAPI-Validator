package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;

class EmailSender {
    private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());

    static void sendEmail(final String message, final String subject, final String emailAddress, Attachment attachment) {

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
                addressFrom = new InternetAddress(Config.get("fromEmailAddress"),
                        Config.get("fromPersonalName"));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
                LOGGER.severe(emailAddress + " : " + e.getMessage());
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

            if (attachment != null) {
                multipart.addBodyPart(attachment.getMailPart());
            }

            mail.setContent(multipart);
            Transport.send(mail);
            //System.out.println(message);

        } catch (final MessagingException e) {
            e.printStackTrace();

            LOGGER.severe(emailAddress + " : " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    static void sendEmail(final String message, final String subject, final String emailAddress) {
        sendEmail(message, subject, emailAddress, null);
    }
}
