package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.Decoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationProcessorTest {

    @Mock
    private AuthenticationHandler handler;
    @Mock
    private SmtpInput smtpInput;
    @Mock
    private GrizzlySmtpSession session;
    @InjectMocks
    private AuthenticationProcessor processor;

    @Before
    public void init() throws Exception {
        when(session.getAuthenticationHandler()).thenReturn(handler);
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        when(smtpInput.hasEnoughData(any(Decoder.class))).thenReturn(true);
        when(smtpInput.getData(any(Decoder.class))).thenReturn("auth");
        when(handler.processAuth(eq("auth"))).thenReturn(false);

        processor.process(smtpInput, session);
        verify(handler, only()).processAuth(eq("auth"));
    }

    @Test
    public void testProcessWithEmptySmtpInput() throws Exception {
        processor.process(smtpInput, session);
        verifyZeroInteractions(handler);
    }

    @Test(expected = SmtpException.class)
    public void testProcessWithSmtpException() throws Exception {
        when(smtpInput.hasEnoughData(any(Decoder.class))).thenReturn(true);
        when(handler.processAuth(anyString())).thenThrow(SmtpException.class);
        try {
            processor.process(smtpInput, session);
        } catch (SmtpException ex) {
            verify(session).setAuthenticationHandler(Matchers.isNull(AuthenticationHandler.class));
            throw ex;
        }
    }
}