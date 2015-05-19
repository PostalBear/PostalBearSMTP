/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
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
public class ResetCommandTest {

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private ResetCommand command = new ResetCommand();

    @Test
    public void testHandle() throws Exception {
        command.handle("RSET", session, input);
        verify(session).resetMailTransaction();
        verify(session).sendResponse(eq(250), eq("OK"));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("RSET", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-RSET command, to reset current SMTP session state."));
    }
}
