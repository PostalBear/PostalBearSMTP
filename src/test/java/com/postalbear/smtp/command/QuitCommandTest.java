/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class QuitCommandTest {

    @Mock
    private SmtpSession session;

    private QuitCommand command = new QuitCommand();

    @Test
    public void testHandleValid() throws Exception {
        SmtpServerConfiguration configuration = Mockito.mock(SmtpServerConfiguration.class);
        when(session.getConfiguration()).thenReturn(configuration);
        when(configuration.getHostName()).thenReturn("localhost");

        command.handle("QUIT", session);
        verify(session).sendResponse(eq(221), eq("localhost Bye"));
        verify(session).closeSession();
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("QUIT", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-QUIT command, to close current SMTP session."));
    }
}
