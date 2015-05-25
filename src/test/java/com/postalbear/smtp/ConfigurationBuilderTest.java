package com.postalbear.smtp;

import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * This dummy test performs basic checks for SmtpServerConfiguration.Builder class.
 *
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationBuilderTest {

    public static final String SOFTWARE_NAME = "SMTP server";
    public static final String HOSTNAME = "localhost";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private AuthenticationHandlerFactory authenticationFactory;
    @Mock
    private SmtpTransactionHandlerFactory handlerFactory;
    @Mock
    private SSLEngineConfigurator sslConfig;

    private SmtpServerConfiguration.Builder builder;

    @Before
    public void init() throws Exception {
        builder = SmtpServerConfiguration.getBuilder();
        builder.setSoftwareName(SOFTWARE_NAME);
        builder.setHostName(HOSTNAME);
        //auth
        builder.setAuthenticationFactory(authenticationFactory);
        builder.setAuthenticationEnforced(true);
        //
        builder.setHandlerFactory(handlerFactory);
        builder.setMaxMessageSize(1024);
        builder.setMaxRecipients(1);
        //security
        builder.setSmtpsEnabled(false);
        builder.setStartTlsEnabled(false);
        builder.setStartTlsEnforced(false);
        builder.setSslConfiguration(sslConfig);
    }

    @Test
    public void testBuildConfiguration() throws Exception {
        SmtpServerConfiguration result = builder.buildConfiguration();

        assertEquals(SOFTWARE_NAME, result.getSoftwareName());
        assertEquals(HOSTNAME, result.getHostName());
        //auth
        assertNotNull(result.getAuthenticationFactory());
        assertTrue(result.isAuthenticationEnforced());
        //
        assertEquals(handlerFactory, result.getHandlerFactory());
        assertEquals(1024, result.getMaxMessageSize());
        assertEquals(1, result.getMaxRecipients());
        //security
        assertFalse(result.isSmtpsEnabled());
        assertFalse(result.isStartTlsEnabled());
        assertFalse(result.isStartTlsEnforced());
        assertEquals(sslConfig, result.getSslConfig());
    }

    @Test
    public void testNullSoftwareName() throws Exception {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("SoftwareName must be set");

        builder.setSoftwareName(null);
        builder.buildConfiguration();
    }

    @Test
    public void testEmptySoftwareName() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("SoftwareName must be set");

        builder.setSoftwareName("");
        builder.buildConfiguration();
    }

    @Test
    public void testNullHostName() throws Exception {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Hostname must be set");

        builder.setHostName(null);
        builder.buildConfiguration();
    }

    @Test
    public void testEmptyHostName() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Hostname must be set");

        builder.setHostName("");
        builder.buildConfiguration();
    }

    @Test
    public void testNullHandlerFactory() throws Exception {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("HandlerFactory can't be null");

        builder.setHandlerFactory(null);
        builder.buildConfiguration();
    }

    @Test
    public void testNegativeMessageSize() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("MaxMessageSize should be >= 0");

        builder.setMaxMessageSize(-1);
        builder.buildConfiguration();
    }

    @Test
    public void testNegativeMaxRecipients() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("MaxRecipients should be >= 0");

        builder.setMaxRecipients(-1);
        builder.buildConfiguration();
    }

    @Test
    public void testEnforcedWithNullAuthenticationFactory() throws Exception {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Authentication can't be enforced when AuthenticationFactory is not set");

        builder.setAuthenticationFactory(null);
        builder.buildConfiguration();
    }

    @Test
    public void testSmtpsWithNullConfig() throws Exception {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("SSL configuration must be set when SMTPS enabled");

        builder.setSmtpsEnabled(true);
        builder.setSslConfiguration(null);
        builder.buildConfiguration();
    }

    @Test
    public void testSmtpsWithStartTlsEnabled() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("StartTLS can't be enabled for SMTPS server");

        builder.setSmtpsEnabled(true);
        builder.setStartTlsEnabled(true);
        builder.buildConfiguration();
    }

    @Test
    public void testSmtpsWithStartTlsEnforced() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("StartTLS can't be enforced for SMTPS server");

        builder.setSmtpsEnabled(true);
        builder.setStartTlsEnforced(true);
        builder.buildConfiguration();
    }

    @Test
    public void testStartTlsWithNullConfig() throws Exception {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("SSL configuration must be set when STARTTLS enabled");

        builder.setStartTlsEnabled(true);
        builder.setSslConfiguration(null);
        builder.buildConfiguration();
    }

    @Test
    public void testStartTlsEnforced() throws Exception {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("StartTLS can't be enforced when not enabled");

        builder.setStartTlsEnabled(false);
        builder.setStartTlsEnforced(true);
        builder.buildConfiguration();
    }
}