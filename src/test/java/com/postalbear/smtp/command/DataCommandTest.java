/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static com.postalbear.smtp.SmtpConstants.CRLF;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCommandTest {

    @Mock
    private SmtpSession session;

    private DataCommand command = new DataCommand();

    @Before
    public void init() {
        when(session.isMailTransactionInProgress()).thenReturn(true);
        when(session.getRecipientsCount()).thenReturn(1);
    }

    @Test(expected = SmtpException.class)
    public void testTransactionNotInProgress() throws Exception {
        when(session.isMailTransactionInProgress()).thenReturn(false);
        try {
            command.handle("DATA", session);
        } catch (SmtpException ex) {
            assertEquals(503, ex.getResponseCode());
            assertEquals("5.5.1 Invalid command sequence: need MAIL command", ex.getResponseMessage());
            throw ex;
        }
    }

    @Ignore("DATA command should be rewritten in NIO way")
    @Test(expected = SmtpException.class)
    public void testNoRecipients() throws Exception {
        when(session.getRecipientsCount()).thenReturn(0);
        try {
            command.handle("DATA", session);
        } catch (SmtpException ex) {
            assertEquals(503, ex.getResponseCode());
            assertEquals("5.5.1 Invalid command sequence: need RCPT command", ex.getResponseMessage());
            throw ex;
        }
    }

    @Ignore("DATA command should be rewritten in NIO way")
    @Test(expected = SmtpException.class)
    public void testHandleInvalidData() throws Exception {
        try {
            command.handle("DATA", session);
        } catch (SmtpException ex) {
            assertEquals(554, ex.getResponseCode());
            assertEquals("Pre-mature end of <CRLF>.<CRLF> terminated data", ex.getResponseMessage());

            verify(session).isMailTransactionInProgress();
            verify(session).getRecipientsCount();
            verify(session).sendResponse(eq(354), eq("End data with <CR><LF>.<CR><LF>"));
            //verify data processing
            verify(session).data(isA(InputStream.class));
            //
            verify(session, never()).resetMailTransaction();
            verify(session, never()).sendResponse(eq(250), eq("OK"));

            throw ex;
        }
    }

    @Ignore("DATA command should be rewritten in NIO way")
    @Test
    public void testHandle() throws Exception {
        InputStream inputStream = IOUtils.toInputStream("data." + CRLF + "." + CRLF + "MAIL FROM:<user@domain.com>");

        command.handle("DATA", session);
        verify(session).sendResponse(eq(354), eq("End data with <CR><LF>.<CR><LF>"));
        //verify data processing
        verify(session).data(isA(InputStream.class));
        //
        verify(session).resetMailTransaction();
        verify(session).sendResponse(eq(250), eq("OK"));
        //next SMTP command should not be consumed.
        assertEquals("MAIL FROM:<user@domain.com>", IOUtils.toString(inputStream));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("DATA", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-DATA command, to send content of e-mail to server."));
        verify(session).sendResponseAsString(eq("214-End data with <CR><LF>.<CR><LF>"));
    }
}
