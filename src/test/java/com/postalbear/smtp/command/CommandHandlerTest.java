/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Grigory Fadeev
 */
public class CommandHandlerTest {

    private CommandHandler handler = CommandRegistry.getCommandHandler();

    @Test
    public void testParseCommandByLength() {
        Assert.assertNotNull(handler.getCommand("HELO"));
    }

    @Test
    public void testParseWithSpaceTerminator() {
        Assert.assertNotNull(handler.getCommand("MAIL FROM"));
    }

    @Test(expected = SmtpException.class)
    public void testUnknownCommand() throws Exception {
        try {
            handler.getCommand("UNKNOWN");
        } catch (SmtpException ex) {
            assertEquals(500, ex.getResponseCode());
            assertEquals("SMTP command: \"UNKNOWN\" not implemented", ex.getResponseMessage());
            throw ex;
        }
    }
}
