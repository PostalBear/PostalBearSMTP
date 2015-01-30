/*
 */
package com.postalbear.smtp.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Grigory Fadeev
 */
public class SmtpExceptionTest {

    @Test
    public void testGeters() throws Exception {
        SmtpException exception = new SmtpException(500, "Syntax error");
        assertEquals(500, exception.getResponseCode());
        assertEquals("Syntax error", exception.getResponseMessage());
    }
}
