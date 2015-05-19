package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthCommandTest {

    @Mock
    private AuthenticationHandler authHandler;
    @Mock
    private AuthenticationHandlerFactory authenticationFactory;
    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private AuthCommand command = new AuthCommand();

    @Before
    public void init() {
        when(session.isAuthenticated()).thenReturn(false);
        when(configuration.getAuthenticationFactory()).thenReturn(authenticationFactory);
        when(authenticationFactory.create(Matchers.anyString(), eq(session))).thenReturn(authHandler);
    }

    @Test(expected = SmtpException.class)
    public void testAuthenticated() throws Exception {
        when(session.isAuthenticated()).thenReturn(true);
        try {
            command.handle("any string", session, input);
        } catch (SmtpException ex) {
            assertEquals(503, ex.getResponseCode());
            assertEquals("Refusing any other AUTH command.", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("AUTH", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-Service for authentication procedure."));
        verify(session).sendResponseAsString(eq("214-AUTH <mechanism> [initial-response]"));
        verify(session).sendResponseAsString(eq("214-  mechanism = SASL authentication mechanism to use,"));
        verify(session).sendResponseAsString(eq("214-  initial-response = an optional base64-encoded response"));
    }
}
