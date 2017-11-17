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

    public Attachment(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

    public MimeBodyPart getMailPart () throws MessagingException, UnsupportedEncodingException {
        MimeBodyPart attachPart = new MimeBodyPart();
        DataSource ds = new ByteArrayDataSource(content.getBytes("UTF-8"), "application/octet-stream");
        attachPart.setDataHandler(new DataHandler(ds));
        attachPart.setFileName(filename);
        return attachPart;
    }

}
