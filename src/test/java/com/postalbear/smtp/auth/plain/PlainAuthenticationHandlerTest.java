/*
 */
package com.postalbear.smtp.auth.plain;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.io.SmtpLineReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Grigory
 */
@RunWith(MockitoJUnitRunner.class)
public class PlainAuthenticationHandlerTest {

    public static final String NUL = new String(new byte[]{0});

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpLineReader lineReader;
    @Mock
    private CredentialsValidator validator;

    private PlainAuthenticationHandler handler;

    @Before
    public void init() {
        when(validator.validateCredentials(anyString(), anyString())).thenReturn(true);
        handler = new PlainAuthenticationHandler(session, lineReader, validator);
    }

    @Test
    public void testWithInitialSecret() throws Exception {
        String secret = Base64.getEncoder().encodeToString(("authoritation" + NUL + "login" + NUL + "password").getBytes());

        String smtpLine = "AUTH PLAIN " + secret;

        handler.auth(smtpLine);
        verify(validator).validateCredentials(eq("login"), eq("password"));
    }

    @Test
    public void testWithoutInitialSecret() throws Exception {
        String secret = Base64.getEncoder().encodeToString(("authorization" + NUL + "login" + NUL + "password").getBytes());
        when(lineReader.readLine()).thenReturn(secret);

        String smtpLine = "AUTH PLAIN";

        handler.auth(smtpLine);
        verify(validator).validateCredentials(eq("login"), eq("password"));
        verify(session).sendResponse(eq(334), eq("OK"));
    }

    @Test
    public void testCanceled() throws Exception {
        String line = "*"; // canceled
        when(lineReader.readLine()).thenReturn(line);

        String smtpLine = "AUTH PLAIN";
        try {
            handler.auth(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("Authentication canceled by client.", ex.getResponseMessage());
        }
    }

    @Test
    public void testInvalidSecretFormat() throws Exception {
        String secret = Base64.getEncoder().encodeToString(("authoritation login" + NUL + "password").getBytes());
        String smtpLine = "AUTH PLAIN " + secret;
        try {
            handler.auth(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument, does not contain NUL", ex.getResponseMessage());
        }
    }

    @Test
    public void testInvalidSecretBase64() throws Exception {
        String smtpLine = "AUTH PLAIN " + "=AAA==";
        try {
            handler.auth(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Invalid command argument, not a valid Base64 string", ex.getResponseMessage());
        }
    }

    @Test
    public void testInvalidCredentials() throws Exception {
        when(validator.validateCredentials(eq("login"), eq("password"))).thenReturn(false);
        String secret = Base64.getEncoder().encodeToString(("authoritation" + NUL + "login" + NUL + "password").getBytes());
        String smtpLine = "AUTH PLAIN " + secret;
        try {
            handler.auth(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(535, ex.getResponseCode());
            assertEquals("5.7.8 Authentication failure, invalid credentials", ex.getResponseMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullSession() {
        new PlainAuthenticationHandler(null, lineReader, validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullLineReader() {
        new PlainAuthenticationHandler(session, null, validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullValidator() {
        new PlainAuthenticationHandler(session, lineReader, null);
    }
}
