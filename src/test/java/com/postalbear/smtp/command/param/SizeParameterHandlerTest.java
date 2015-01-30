/*
 */
package com.postalbear.smtp.command.param;

import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class SizeParameterHandlerTest {

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpServerConfiguration configuration;

    private final SizeParameterHandler sizeHandler = new SizeParameterHandler();

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void testMatch() {
        assertTrue(sizeHandler.match("size"));
        assertTrue(sizeHandler.match("size=5000"));
    }

    @Test
    public void testNotMatch() {
        assertFalse(sizeHandler.match("test"));
    }

    @Test
    public void testInvalidSyntax() {
        try {
            sizeHandler.handleLine(session, "test");
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("incorrect syntax of SIZE parameter", ex.getResponseMessage());
        }
    }

    @Test
    public void testInvalidSizeValue() {
        try {
            sizeHandler.handleLine(session, "size=test");
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("failed to get value of SIZE parameter", ex.getResponseMessage());
        }
    }

    @Test
    public void testMaxSizeExceeded() {
        when(configuration.getMaxMessageSize()).thenReturn(1);
        try {
            sizeHandler.handleLine(session, "size=1000");
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(552, ex.getResponseCode());
            assertEquals("5.3.4 Message size exceeds fixed limit", ex.getResponseMessage());
        } finally {
            verify(session).getConfiguration();
            verify(session, never()).messageSize(anyInt());
            verify(configuration).getMaxMessageSize();
            verifyNoMoreInteractions(session, configuration);
        }
    }

    @Test
    public void testHandleLine() {
        when(configuration.getMaxMessageSize()).thenReturn(1000);
        sizeHandler.handleLine(session, "size=1000");
        verify(session).messageSize(eq(1000));
    }
}
