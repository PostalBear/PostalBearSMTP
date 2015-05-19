/*
 */
package com.postalbear.smtp.auth.plain;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory
 */
@RunWith(MockitoJUnitRunner.class)
public class PlainAuthenticationHandlerTest {

    public static final String NUL = new String(new byte[]{0});
    public static final String CANCEL_COMMAND = "*";

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput smtpInput;
    @Mock
    private CredentialsValidator validator;
    @InjectMocks
    private PlainAuthenticationHandler handler;

    @Before
    public void init() {
        when(validator.validateCredentials(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void testWithInitialSecret() throws Exception {
        String secret = Base64.getEncoder().encodeToString(("authoritation" + NUL + "login" + NUL + "password").getBytes());
        String smtpLine = "AUTH PLAIN " + secret;

        handler.start(smtpLine);
        verify(validator).validateCredentials(eq("login"), eq("password"));
        verify(session).setAuthenticated();
    }

    @Test
    public void testWithoutInitialSecret() throws Exception {
        String smtpLine = "AUTH PLAIN";
        handler.start(smtpLine);

        String secret = Base64.getEncoder().encodeToString(("authorization" + NUL + "login" + NUL + "password").getBytes());
        when(smtpInput.getSmtpLine()).thenReturn(secret);
        handler.process(smtpInput, session);
        verify(validator).validateCredentials(eq("login"), eq("password"));
        verify(session).sendResponse(eq(334), eq("OK"));
        verify(session).setAuthenticated();
    }

    @Test
    public void testCanceled() throws Exception {
        String smtpLine = "AUTH PLAIN";
        handler.start(smtpLine);

        String line = CANCEL_COMMAND; // canceled
        when(smtpInput.getSmtpLine()).thenReturn(line);
        handler.process(smtpInput, session);
        verify(session).sendResponse(eq(501), eq("Authentication canceled by client."));
        verify(validator, never()).validateCredentials(anyString(), anyString());
        verify(session, never()).setAuthenticated();
    }

    @Test(expected = SmtpException.class)
    public void testInvalidSecretFormat() throws Exception {
        String secret = Base64.getEncoder().encodeToString(("authoritation login" + NUL + "password").getBytes());
        String smtpLine = "AUTH PLAIN " + secret;
        try {
            handler.start(smtpLine);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument, does not contain NUL", ex.getResponseMessage());
            verify(session, never()).setAuthenticated();
            throw ex;
        }
    }

    @Test(expected = SmtpException.class)
    public void testInvalidSecretBase64() throws Exception {
        String smtpLine = "AUTH PLAIN " + "=AAA==";
        try {
            handler.start(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument, not a valid Base64 string", ex.getResponseMessage());
            verify(session, never()).setAuthenticated();
            throw ex;
        }
    }

    @Test(expected = SmtpException.class)
    public void testInvalidCredentials() throws Exception {
        when(validator.validateCredentials(eq("login"), eq("password"))).thenReturn(false);
        String secret = Base64.getEncoder().encodeToString(("authoritation" + NUL + "login" + NUL + "password").getBytes());
        String smtpLine = "AUTH PLAIN " + secret;
        try {
            handler.start(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(535, ex.getResponseCode());
            assertEquals("5.7.8 Authentication failure, invalid credentials", ex.getResponseMessage());
            verify(session, never()).setAuthenticated();
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullSession() {
        new PlainAuthenticationHandler(null, validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullValidator() {
        new PlainAuthenticationHandler(session, null);
    }
}
