package de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;
import java.io.UnsupportedEncodingException;

public class Attachment {
    private String filename;
    private String content;
    private String type;

    public Attachment(String filename, String content, String type) {

    }

    public MimeBodyPart getMailPart () throws MessagingException, UnsupportedEncodingException {
        MimeBodyPart attachPart = new MimeBodyPart();
        DataSource ds = new ByteArrayDataSource(content.getBytes("UTF-8"), type);
        attachPart.setDataHandler(new DataHandler(ds));
        attachPart.setFileName(filename);
        return attachPart;
    }

}
