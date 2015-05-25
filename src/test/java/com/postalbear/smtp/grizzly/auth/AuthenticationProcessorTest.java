package com.postalbear.smtp.grizzly.auth;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpProcessor;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.codec.Decoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationProcessorTest {

    @Mock
    private AuthenticationHandler delegate;
    @Mock
    private SmtpInput smtpInput;
    @Mock
    private SmtpSession session;
    @InjectMocks
    private AuthenticationProcessor processor;

    @Test
    public void testProcessAuthentication() throws Exception {
        when(delegate.processAuthentication(eq("Initial line"))).thenReturn(true);
        assertTrue(processor.processAuthentication("Initial line"));
        verify(session).setSmtpProcessor(eq(processor));

        when(delegate.processAuthentication(eq("last line"))).thenReturn(false);
        assertFalse(processor.processAuthentication("last line"));
        verifyNoMoreInteractions(session);
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        when(smtpInput.hasEnoughData(any(Decoder.class))).thenReturn(true, false);
        when(smtpInput.getData(any(Decoder.class))).thenReturn("auth");
        when(delegate.processAuthentication(eq("auth"))).thenReturn(false);

        processor.process(smtpInput, session);
        verify(session).setSmtpProcessor(Matchers.isNull(SmtpProcessor.class));
        verifyNoMoreInteractions(session);
    }

    @Test
    public void testProcessWithEmptySmtpInput() throws Exception {
        processor.process(smtpInput, session);
        verifyZeroInteractions(session, delegate);
    }

    @Test(expected = SmtpException.class)
    public void testProcessWithSmtpException() throws Exception {
        when(smtpInput.hasEnoughData(any(Decoder.class))).thenReturn(true);
        when(delegate.processAuthentication(anyString())).thenThrow(SmtpException.class);
        try {
            processor.process(smtpInput, session);
        } catch (SmtpException ex) {
            verify(session).setSmtpProcessor(Matchers.isNull(SmtpProcessor.class));
            throw ex;
        }
    }
}