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
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class StartTLSCommandTest {

    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private final StartTLSCommand command = new StartTLSCommand();

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
        //
        when(configuration.isStartTlsEnabled()).thenReturn(true);
    }

    @Test(expected = SmtpException.class)
    public void testInvalidSyntax() throws Exception {
        try {
            command.handle("STARTTLS unexpected text", session, input);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("Syntax error (no parameters allowed)", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test(expected = SmtpException.class)
    public void testNotAllowedByConfiguration() throws Exception {
        when(configuration.isStartTlsEnabled()).thenReturn(false);
        try {
            command.handle("STARTTLS", session, input);
        } catch (SmtpException ex) {
            assertEquals(454, ex.getResponseCode());
            assertEquals("TLS not supported", ex.getResponseMessage());
            verify(session, never()).startTls();
            throw ex;
        }
    }

    @Test(expected = SmtpException.class)
    public void testTlsAlreadyInProgress() throws Exception {
        when(session.isConnectionSecured()).thenReturn(true);
        try {
            command.handle("STARTTLS", session, input);
        } catch (SmtpException ex) {
            assertEquals(454, ex.getResponseCode());
            assertEquals("TLS not available due to temporary reason: TLS already active", ex.getResponseMessage());
            verify(session, never()).startTls();
            throw ex;
        }
    }

    @Test
    public void testHandle() throws Exception {
        command.handle("STARTTLS", session, input);
        verify(session).sendResponse(eq(220), eq("Ready to start TLS"));
        verify(session).startTls();
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("STARTTLS", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-STARTTLS command, to ask server for transport encryption."));
    }
}
