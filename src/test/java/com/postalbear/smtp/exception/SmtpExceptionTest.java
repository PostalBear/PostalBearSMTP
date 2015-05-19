/*
 */
package com.postalbear.smtp.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Grigory Fadeev
 */
public class SmtpExceptionTest {

    private SmtpException exception = new SmtpException(500, "Syntax error");

    @Test
    public void testGetResponseCode() throws Exception {
        assertEquals(500, exception.getResponseCode());
    }

    @Test
    public void testGetResponseMessage() throws Exception {
        assertEquals("Syntax error", exception.getResponseMessage());
    }
}
