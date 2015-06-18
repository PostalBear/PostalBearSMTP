package com.postalbear.smtp;

import com.sun.mail.smtp.SMTPMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.mail.Session;

import static javax.mail.Session.getInstance;

/**
 * Test checks SMTP basics.
 *
 * @author Grigory Fadeev
 */
public class SmtpServerIT extends AbstractServerIT {

    @Test
    public void testPlainSmtp() throws Exception {
        createServer(SmtpServerConfiguration.getBuilder());
        Session session = getInstance(getSessionProperties("smtp"));
        SMTPMessage message = new SMTPMessage(session, IOUtils.toInputStream(MESSAGE_CONTENT));
        message.setEnvelopeFrom(sender.toString());
        sendMessage(session, message, recipient);

        assertFirstSmtpTransaction();
    }
}
