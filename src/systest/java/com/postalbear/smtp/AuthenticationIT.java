package com.postalbear.smtp;

import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.auth.MultipleAuthenticationHandlerFactory;
import com.postalbear.smtp.auth.login.LoginAuthenticationHandlerFactory;
import com.postalbear.smtp.auth.plain.PlainAuthenticationHandlerFactory;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPSendFailedException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.mail.*;
import java.util.Properties;

import static javax.mail.Session.getInstance;
import static org.apache.commons.lang3.StringUtils.chomp;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test checks that PostalBear are capable to deal with SMTP AUTH (at least with Java Mail).
 *
 * @author Grigory Fadeev
 */
public class AuthenticationIT extends AbstractServerIT {

    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";

    @Mock
    private CredentialsValidator validator;

    @Before
    public void init() throws Exception {
        when(validator.validateCredentials(eq(LOGIN), eq(PASSWORD))).thenReturn(true);
    }

    @Test
    public void testAuthLogin() throws Exception {
        SmtpServerConfiguration.Builder builder = SmtpServerConfiguration.getBuilder();
        builder.setAuthenticationEnforced(true);
        builder.setAuthenticationFactory(
                new MultipleAuthenticationHandlerFactory(
                        new LoginAuthenticationHandlerFactory(validator)));
        createServer(builder);

        Properties properties = getSessionProperties("smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.auth.mechanisms", "LOGIN");
        Session session = getInstance(properties, new TestAuthenticator());

        SMTPMessage message = new SMTPMessage(session, IOUtils.toInputStream(MESSAGE_CONTENT));
        message.setEnvelopeFrom(sender.toString());
        sendMessage(session, message, recipient);

        verify(validator).validateCredentials(eq(LOGIN), eq(PASSWORD));
        assertFirstSmtpTransaction();
    }

    @Test
    public void testAuthPlain() throws Exception {
        SmtpServerConfiguration.Builder builder = SmtpServerConfiguration.getBuilder();
        builder.setAuthenticationEnforced(true);
        builder.setAuthenticationFactory(
                new MultipleAuthenticationHandlerFactory(
                        new PlainAuthenticationHandlerFactory(validator)));
        createServer(builder);

        Properties properties = getSessionProperties("smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.auth.mechanisms", "PLAIN");
        Session session = getInstance(properties, new TestAuthenticator());

        SMTPMessage message = new SMTPMessage(session, IOUtils.toInputStream(MESSAGE_CONTENT));
        message.setEnvelopeFrom(sender.toString());
        sendMessage(session, message, recipient);

        verify(validator).validateCredentials(eq(LOGIN), eq(PASSWORD));
        assertFirstSmtpTransaction();
    }

    @Test(expected = SMTPSendFailedException.class)
    public void testAuthEnforced() throws Exception {
        SmtpServerConfiguration.Builder builder = SmtpServerConfiguration.getBuilder();
        builder.setAuthenticationEnforced(true);
        builder.setAuthenticationFactory(
                new MultipleAuthenticationHandlerFactory(
                        new LoginAuthenticationHandlerFactory(validator),
                        new PlainAuthenticationHandlerFactory(validator)));
        createServer(builder);

        Session session = getInstance(getSessionProperties("smtp"));

        SMTPMessage message = new SMTPMessage(session, IOUtils.toInputStream(MESSAGE_CONTENT));
        message.setEnvelopeFrom(sender.toString());
        try {
            sendMessage(session, message, recipient);
        } catch (SMTPSendFailedException ex) {
            //SMTPTransport adds \n at the end of server response
            assertEquals("530 5.7.0 Authentication required", chomp(ex.getMessage()));
            throw ex;
        }
    }

    @Test(expected = AuthenticationFailedException.class)
    public void testAuthNotSupportedMechanism() throws Exception {
        SmtpServerConfiguration.Builder builder = SmtpServerConfiguration.getBuilder();
        builder.setAuthenticationEnforced(true);
        builder.setAuthenticationFactory(
                new MultipleAuthenticationHandlerFactory(
                        new LoginAuthenticationHandlerFactory(validator),
                        new PlainAuthenticationHandlerFactory(validator)));
        createServer(builder);

        Properties properties = getSessionProperties("smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.auth.mechanisms", "DIGEST-MD5");

        Session session = getInstance(properties, new TestAuthenticator());
        Transport transport = session.getTransport();
        try {
            transport.connect();
        } catch (AuthenticationFailedException ex) {
            assertEquals("No authentication mechansims supported by both server and client", ex.getMessage());
            throw ex;
        } finally {
            transport.close();
        }
    }

    private static class TestAuthenticator extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(LOGIN, PASSWORD);
        }
    }
}
