/*
 */
package com.postalbear.smtp.auth.login;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.io.SmtpLineReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Grigory
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginAuthenticationHandlerTest {

    private static final String LOGIN_ENCRYPTED = "bG9naW4=";
    private static final String LOGIN = "login";

    private static final String PASSWORD_ENCRYPTED = "cGFzc3dvcmQ=";
    private static final String PASSWORD = "password";

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpLineReader lineReader;
    @Mock
    private CredentialsValidator validator;
    private LoginAuthenticationHandler handler;

    @Before
    public void init() {
        when(validator.validateCredentials(anyString(), anyString())).thenReturn(true);
        handler = new LoginAuthenticationHandler(session, lineReader, validator);
    }

    @Test
    public void testWithoutInitialSecret() throws Exception {
        String smtpLine = "AUTH LOGIN";
        when(lineReader.readLine()).thenReturn(LOGIN_ENCRYPTED, PASSWORD_ENCRYPTED);
        handler.auth(smtpLine);
        verify(session).sendResponse(eq(334), eq("VXNlcm5hbWU6"));
        verify(session).sendResponse(eq(334), eq("UGFzc3dvcmQ6"));
        verify(validator).validateCredentials(eq(LOGIN), eq(PASSWORD));
    }

    @Test
    public void testWithInitialSecret() throws Exception {
        String smtpLine = "AUTH LOGIN " + LOGIN_ENCRYPTED;
        when(lineReader.readLine()).thenReturn(PASSWORD_ENCRYPTED);
        handler.auth(smtpLine);
        verify(session).sendResponse(eq(334), eq("UGFzc3dvcmQ6"));
        verify(validator).validateCredentials(eq(LOGIN), eq(PASSWORD));
    }

    @Test
    public void testWithIncorrectUsernameFormat() throws Exception {
        String smtpLine = "AUTH LOGIN " + "=AAA==";
        try {
            handler.auth(smtpLine);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument: Username - not a valid Base64 string", ex.getResponseMessage());
        }
    }

    @Test
    public void testWithIncorrectPasswordFormat() throws Exception {
        String smtpLine = "AUTH LOGIN " + LOGIN_ENCRYPTED;
        when(lineReader.readLine()).thenReturn("=AAA==");
        try {
            handler.auth(smtpLine);
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument: Password - not a valid Base64 string", ex.getResponseMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullSession() {
        new LoginAuthenticationHandler(null, lineReader, validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullLineReader() {
        new LoginAuthenticationHandler(session, null, validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullValidator() {
        new LoginAuthenticationHandler(session, lineReader, null);
    }
}
