/*
 */
package com.postalbear.smtp.command;

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
public class ExpandCommandTest {

    @Mock
    private SmtpSession session;

    private ExpandCommand command = new ExpandCommand();

    @Test(expected = SmtpException.class)
    public void testHandle() throws Exception {
        try {
            command.handle("EXPN", session);
        } catch (SmtpException ex) {
            assertEquals(502, ex.getResponseCode());
            assertEquals("EXPN command is not supported.", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("EXPN", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-EXPN command is not supported."));
    }
}
