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
public class HeloCommandTest {

    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private HeloCommand command = new HeloCommand();

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
    }

    @Test(expected = SmtpException.class)
    public void testInvalidSyntax() throws Exception {
        try {
            command.handle("HELO", session, input);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Syntax error: HELO <hostname>", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testHandle() throws Exception {
        when(configuration.getHostName()).thenReturn("localhost");
        command.handle("HELO smtpclient", session, input);
        verify(session).sendResponse(eq(250), eq("localhost"));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("HELO", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-Used by client to introduce itself."));
        verify(session).sendResponseAsString(eq("214-HELO <hostname>"));
    }
}
