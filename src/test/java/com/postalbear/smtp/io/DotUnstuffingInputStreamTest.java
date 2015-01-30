/*
 */
package com.postalbear.smtp.io;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static com.postalbear.smtp.SmtpConstants.CRLF;
import static org.junit.Assert.assertEquals;

/**
 * @author Grigory Fadeev
 */
public class DotUnstuffingInputStreamTest {

    @Test
    public void testPlainString() throws Exception {
        DotUnstuffingInputStream stream = new DotUnstuffingInputStream(IOUtils.toInputStream("data"));
        assertEquals("data", IOUtils.toString(stream));
    }

    @Test
    public void testDotUnstuffing() throws Exception {
        DotUnstuffingInputStream stream = new DotUnstuffingInputStream(IOUtils.toInputStream(CRLF + ".." + CRLF));
        assertEquals(CRLF + "." + CRLF, IOUtils.toString(stream));
    }
}
