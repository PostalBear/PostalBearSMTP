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
public class CommandRegistryTest {

    private CommandRegistry commandRegistry = CommandRegistryFactory.create();

    @Test
    public void testParseCommandByLength() {
        Assert.assertNotNull(commandRegistry.getCommand("HELO"));
    }

    @Test
    public void testParseWithSpaceTerminator() {
        Assert.assertNotNull(commandRegistry.getCommand("MAIL FROM"));
    }

    @Test(expected = SmtpException.class)
    public void testUnknownCommand() throws Exception {
        try {
            commandRegistry.getCommand("UNKNOWN");
        } catch (SmtpException ex) {
            assertEquals(500, ex.getResponseCode());
            assertEquals("SMTP command: \"UNKNOWN\" not implemented", ex.getResponseMessage());
            throw ex;
        }
    }
}
