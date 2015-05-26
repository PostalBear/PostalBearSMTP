/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class NoopCommandTest {

    @Mock
    private SmtpSession session;

    private NoopCommand command = new NoopCommand();

    @Test
    public void testHandleValid() throws Exception {
        command.handle("NOOP", session);
        verify(session).sendResponse(eq(250), eq("OK"));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("NOOP", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-NOOP command, simply do nothing."));
    }
}
