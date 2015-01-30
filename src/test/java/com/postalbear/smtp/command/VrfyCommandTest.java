/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class VrfyCommandTest {

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private final VerifyCommand command = new VerifyCommand();

    @Test(expected = SmtpException.class)
    public void testHandle() throws Exception {
        try {
            command.handle("VRFY", session, input);
        } catch (SmtpException ex) {
            assertEquals(502, ex.getResponseCode());
            assertEquals("VRFY command is disabled", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("VRFY", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-VRFY command is not supported."));
    }
}
