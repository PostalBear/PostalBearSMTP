/*
 */
package com.postalbear.smtp.io;

import com.postalbear.smtp.exception.SmtpException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static com.postalbear.smtp.SmtpConstants.CRLF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Grigory Fadeev
 */
public class DotTerminatedInputStreamTest {

    @Test
    public void testEmptyInputWithMissedTerminator() throws Exception {
        DotTerminatedInputStream stream = new DotTerminatedInputStream(IOUtils.toInputStream(""));
        try {
            IOUtils.toString(stream);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(554, ex.getResponseCode());
            assertEquals("Pre-mature end of <CRLF>.<CRLF> terminated data", ex.getResponseMessage());
        }
    }

    @Test
    public void testMissedTerminator() throws Exception {
        DotTerminatedInputStream stream = new DotTerminatedInputStream(IOUtils.toInputStream("data" + CRLF));
        try {
            IOUtils.toString(stream);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(554, ex.getResponseCode());
            assertEquals("Pre-mature end of <CRLF>.<CRLF> terminated data", ex.getResponseMessage());
        }
    }

    @Test
    public void testInputWithCRLF() throws Exception {
        DotTerminatedInputStream stream = new DotTerminatedInputStream(IOUtils.toInputStream(CRLF + "data." + CRLF + "." + CRLF));
        assertEquals(CRLF + "data." + CRLF, IOUtils.toString(stream));
    }

    @Test
    public void testDotStuffing() throws Exception {
        DotTerminatedInputStream stream = new DotTerminatedInputStream(IOUtils.toInputStream(".." + CRLF + "." + CRLF));
        assertEquals(".." + CRLF, IOUtils.toString(stream));
    }

    @Test
    public void testEmptyInput() throws Exception {
        DotTerminatedInputStream stream = new DotTerminatedInputStream(IOUtils.toInputStream(CRLF + "." + CRLF));
        assertEquals(CRLF, IOUtils.toString(stream));
    }

    @Test
    public void testValidInput() throws Exception {
        DotTerminatedInputStream stream = new DotTerminatedInputStream(IOUtils.toInputStream("data" + CRLF + "." + CRLF));
        assertEquals("data" + CRLF, IOUtils.toString(stream));
    }
}
