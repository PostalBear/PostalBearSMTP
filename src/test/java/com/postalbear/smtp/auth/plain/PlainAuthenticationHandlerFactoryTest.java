package com.postalbear.smtp.auth.plain;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.auth.login.LoginAuthenticationHandlerFactory;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * This is quite dumb test, it checks basic negative cases.
 * More deep handler testing is subject for other tests.
 *
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlainAuthenticationHandlerFactoryTest {

    @Mock
    private CredentialsValidator credentialsValidator;
    @Mock
    private SmtpSession session;
    @InjectMocks
    private PlainAuthenticationHandlerFactory factory;

    @Test
    public void testGetAuthenticationMechanisms() throws Exception {
        assertEquals(Collections.singleton("PLAIN"), factory.getAuthenticationMechanisms());
    }

    @Test(expected = SmtpException.class)
    public void testCreateWithUnknownMechanism() throws Exception {
        try {
            factory.create("UNKNOWN", session);
        } catch (SmtpException ex) {
            assertEquals(504, ex.getResponseCode());
            assertEquals("5.5.4 The requested authentication mechanism is not supported", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullCredentialValidator() throws Exception {
        new LoginAuthenticationHandlerFactory(null);
    }
}