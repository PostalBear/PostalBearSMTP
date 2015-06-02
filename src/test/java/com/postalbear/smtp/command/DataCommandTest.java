/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.data.DataHandlerFactory;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCommandTest {

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private DataHandlerFactory dataHandlerFactory;
    @Mock
    private DataHandler dataHandler;

    private DataCommand command = new DataCommand();

    @Before
    public void init() {
        when(session.isMailTransactionInProgress()).thenReturn(true);
        when(session.getRecipientsCount()).thenReturn(1);

        when(session.getConfiguration()).thenReturn(configuration);
        when(configuration.getDataHandlerFactory()).thenReturn(dataHandlerFactory);
        when(dataHandlerFactory.create(eq(session))).thenReturn(dataHandler);
    }

    @Test(expected = SmtpException.class)
    public void testTransactionNotInProgress() throws Exception {
        when(session.isMailTransactionInProgress()).thenReturn(false);
        try {
            command.handle("DATA", session);
        } catch (SmtpException ex) {
            verify(dataHandler, never()).kickstartData();
            assertEquals(503, ex.getResponseCode());
            assertEquals("5.5.1 Invalid command sequence: need MAIL command", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test(expected = SmtpException.class)
    public void testNoRecipients() throws Exception {
        when(session.getRecipientsCount()).thenReturn(0);
        try {
            command.handle("DATA", session);
        } catch (SmtpException ex) {
            verify(dataHandler, never()).kickstartData();
            assertEquals(503, ex.getResponseCode());
            assertEquals("5.5.1 Invalid command sequence: need RCPT command", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testHandle() throws Exception {
        command.handle("DATA", session);
        verify(dataHandler).kickstartData();
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
