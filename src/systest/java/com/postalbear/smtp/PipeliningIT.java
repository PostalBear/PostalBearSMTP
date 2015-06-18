package com.postalbear.smtp;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

import static com.postalbear.smtp.SmtpConstants.CRLF;
import static org.junit.Assert.assertEquals;

/**
 * @author Grigory Fadeev.
 */
public class PipeliningIT extends AbstractServerIT {

    @Test
    public void testSmtpPipelining() throws Exception {
        createServer(SmtpServerConfiguration.getBuilder());

        try (Socket soc = new Socket("localhost", getPort())) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            Writer writer = new PrintWriter(soc.getOutputStream(), true);
            //read server welcome banner
            assertEquals("220 " + getLocalhost() + " ESMTP GrizzlySMTP", reader.readLine());
            //send client helo
            writer.write("EHLO " + CLIENT_HELO + CRLF);
            writer.flush();
            //read server response
            assertEquals("250-" + getLocalhost(), reader.readLine());
            assertEquals("250-8BITMIME", reader.readLine());
            assertEquals("250-PIPELINING", reader.readLine());
            assertEquals("250 OK", reader.readLine());
            //send MAIL FROM and batch of RCPT TO commands
            writer.write("MAIL FROM:<" + sender.getAddress() + ">" + CRLF);
            writer.write("RCPT TO:<" + recipient.getAddress() + ">" + CRLF);
            //and DATA to ask server to flush its response buffer
            writer.write("DATA" + CRLF);
            writer.flush();
            //read server response
            assertEquals("250 sender <" + sender.getAddress() + "> OK", reader.readLine());
            assertEquals("250 recipient <" + recipient.getAddress() + "> OK", reader.readLine());
            assertEquals("354 End data with <CR><LF>.<CR><LF>", reader.readLine());
            //now send mail message immediately followed by new mail transaction
            writer.write(MESSAGE_CONTENT + "." + CRLF);
            writer.write("MAIL FROM:<anotherFrom@test.com>" + CRLF);
            writer.flush();
            //followed by QUIT to flush server buffer and close connection
            writer.write("QUIT" + CRLF);
            writer.flush();
            //read server response
            assertEquals("250 OK", reader.readLine());
            assertEquals("250 sender <anotherFrom@test.com> OK", reader.readLine());
            assertEquals("221 0.0.0.0 Bye", reader.readLine());

            assertFirstSmtpTransaction();
        }
    }
}
