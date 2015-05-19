/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class EhloCommandTest {

    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private EhloCommand command = new EhloCommand();

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
        when(configuration.getHostName()).thenReturn("localhost");
    }

    @Test(expected = SmtpException.class)
    public void testInvalidSyntax() throws Exception {
        try {
            command.handle("EHLO", session, input);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Syntax error: EHLO hostname", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testHandleWithDefaultExtensions() throws Exception {
        command.handle("EHLO mydomain", session, input);

        verify(session).resetMailTransaction();
        verify(session).helo(eq("mydomain"));

        verify(session).sendResponseAsString(eq("250-localhost"));
        verify(session).sendResponseAsString(eq("250-8BITMIME"));
        verify(session).sendResponse(eq(250), eq("OK"));
    }

    @Test
    public void testHandleWithStartTLS() throws Exception {
        when(configuration.isStartTlsEnabled()).thenReturn(true);

        command.handle("EHLO mydomain", session, input);

        verify(session).resetMailTransaction();
        verify(session).helo(eq("mydomain"));

        verify(session).sendResponseAsString(eq("250-localhost"));
        verify(session).sendResponseAsString(eq("250-8BITMIME"));
        verify(session).sendResponseAsString(eq("250-STARTTLS"));
        verify(session).sendResponse(eq(250), eq("OK"));
    }

    @Test
    public void testHandleWithSize() throws Exception {
        when(configuration.getMaxMessageSize()).thenReturn(100);

        command.handle("EHLO mydomain", session, input);

        verify(session).resetMailTransaction();
        verify(session).helo(eq("mydomain"));

        verify(session).sendResponseAsString(eq("250-localhost"));
        verify(session).sendResponseAsString(eq("250-8BITMIME"));
        verify(session).sendResponseAsString(eq("250-SIZE 100"));
        verify(session).sendResponse(eq(250), eq("OK"));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("EHLO", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-Used by client to introduce itself."));
        verify(session).sendResponseAsString(eq("214-EHLO <hostname>"));
    }
}
