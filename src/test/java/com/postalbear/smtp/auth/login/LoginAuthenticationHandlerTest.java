/*
 */
package com.postalbear.smtp.auth.login;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginAuthenticationHandlerTest {

    public static final String CANCEL_COMMAND = "*";

    public static final String LOGIN_REQUEST = "VXNlcm5hbWU6";
    public static final String LOGIN_ENCRYPTED = "bG9naW4=";
    public static final String LOGIN = "login";

    public static final String PASSWORD_REQUEST = "UGFzc3dvcmQ6";
    public static final String PASSWORD_ENCRYPTED = "cGFzc3dvcmQ=";
    public static final String PASSWORD = "password";

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput smtpInput;
    @Mock
    private CredentialsValidator validator;
    @InjectMocks
    private LoginAuthenticationHandler handler;

    @Before
    public void init() {
        when(validator.validateCredentials(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void testWithoutInitialSecret() throws Exception {
        String smtpLine = "AUTH LOGIN";
        handler.start(smtpLine);

        when(smtpInput.hasNextSmtpLine()).thenReturn(true, true, false);
        when(smtpInput.getSmtpLine()).thenReturn(LOGIN_ENCRYPTED, PASSWORD_ENCRYPTED);
        handler.process(smtpInput, session);
        verify(session).sendResponse(eq(334), eq(LOGIN_REQUEST));
        handler.process(smtpInput, session);
        verify(session).sendResponse(eq(334), eq(PASSWORD_REQUEST));
        verify(validator).validateCredentials(eq(LOGIN), eq(PASSWORD));
        verify(session).setAuthenticated();
    }

    @Test
    public void testWithInitialSecret() throws Exception {
        String smtpLine = "AUTH LOGIN " + LOGIN_ENCRYPTED;
        handler.start(smtpLine);

        when(smtpInput.hasNextSmtpLine()).thenReturn(true, false);
        when(smtpInput.getSmtpLine()).thenReturn(PASSWORD_ENCRYPTED);
        handler.process(smtpInput, session);
        verify(session).sendResponse(eq(334), eq("UGFzc3dvcmQ6"));
        verify(validator).validateCredentials(eq(LOGIN), eq(PASSWORD));
        verify(session).setAuthenticated();
    }

    @Test
    public void testCanceled() throws Exception {
        String smtpLine = "AUTH PLAIN";
        handler.start(smtpLine);

        when(smtpInput.hasNextSmtpLine()).thenReturn(true, false);
        when(smtpInput.getSmtpLine()).thenReturn(CANCEL_COMMAND);
        handler.process(smtpInput, session);
        verify(session).sendResponse(eq(501), eq("Authentication canceled by client."));
        verify(validator, never()).validateCredentials(anyString(), anyString());
        verify(session, never()).setAuthenticated();
    }

    @Test(expected = SmtpException.class)
    public void testWithIncorrectUsernameFormat() throws Exception {
        String smtpLine = "AUTH LOGIN =AAA==";
        try {
            handler.start(smtpLine);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument: Username - not a valid Base64 string", ex.getResponseMessage());
            verify(session, never()).setAuthenticated();
            throw ex;
        }
    }

    @Test(expected = SmtpException.class)
    public void testWithIncorrectPasswordFormat() throws Exception {
        String smtpLine = "AUTH LOGIN " + LOGIN_ENCRYPTED;
        handler.start(smtpLine);

        when(smtpInput.hasNextSmtpLine()).thenReturn(true, false);
        when(smtpInput.getSmtpLine()).thenReturn("=AAA==");
        try {
            handler.process(smtpInput, session);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument: Password - not a valid Base64 string", ex.getResponseMessage());
            verify(session, never()).setAuthenticated();
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullSession() {
        new LoginAuthenticationHandler(null, validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullValidator() {
        new LoginAuthenticationHandler(session, null);
    }
}
