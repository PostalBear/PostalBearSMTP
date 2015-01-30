package com.postalbear.smtp;

import com.sun.mail.smtp.SMTPMessage;
import org.apache.commons.io.IOUtils;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.mail.Session;
import java.io.InputStream;
import java.util.Properties;

import static javax.mail.Session.getInstance;

/**
 * Checks transport encryption facilities of PostalBear like STARTTLS extension and SMTPS.
 *
 * @author Grigory Fadeev
 */
public class TransportEncryptionIT extends AbstractServerIT {

    private static SSLEngineConfigurator sslConf;

    @BeforeClass
    public static void preInit() throws Exception {
        try (InputStream keystoreStream = TransportEncryptionIT.class.getResourceAsStream("server_keystore.jks")) {
            SSLContextConfigurator sslCon = new SSLContextConfigurator();
            sslCon.setSecurityProtocol("TLS");
            sslCon.setKeyStoreType("JKS");
            sslCon.setKeyStoreBytes(IOUtils.toByteArray(keystoreStream));
            sslCon.setKeyStorePass("");
            sslConf = new SSLEngineConfigurator(sslCon, false, false, false);
        }
    }

    /**
     * With current implementation of handleAccept method (of SMTPFilter) this test will always fail.
     * Currently server always sends greeting banner even before handshake is done, obviously it's not what client expects.
     *
     * @throws Exception
     */
    @Test
    public void testSmtps() throws Exception {
        SmtpServerConfiguration.Builder builder = getSslAwareConfigurationBuilder();
        builder.setSmtpsEnabled(true);
        createServer(builder);

        Properties properties = getSSLSessionProperties("smtps");
        Session session = getInstance(properties);
        SMTPMessage message = new SMTPMessage(session, IOUtils.toInputStream(MESSAGE_CONTENT));
        message.setEnvelopeFrom(sender.toString());

        sendMessage(session, message, recipient);
        assertMessageReceived();
    }

    @Test
    public void testStartTls() throws Exception {
        SmtpServerConfiguration.Builder builder = getSslAwareConfigurationBuilder();
        builder.setStartTlsEnabled(true);
        builder.setStartTlsEnforced(true);
        createServer(builder);

        Properties properties = getSSLSessionProperties("smtp");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.starttls.required", "true");

        Session session = getInstance(properties);
        SMTPMessage message = new SMTPMessage(session, IOUtils.toInputStream(MESSAGE_CONTENT));
        message.setEnvelopeFrom(sender.toString());

        sendMessage(session, message, recipient);
        assertMessageReceived();
    }

    private Properties getSSLSessionProperties(String protocol) throws Exception {
        Properties properties = getSessionProperties(protocol);
        //It does not make sense to check server certificate in integration tests.
        //So i force java mail to blindly trust whatever server uses.
        //Do not use such stuff in production code ;)
        properties.setProperty("mail." + protocol + ".ssl.trust", "*");
        return properties;
    }

    private SmtpServerConfiguration.Builder getSslAwareConfigurationBuilder() throws Exception {
        SmtpServerConfiguration.Builder builder = SmtpServerConfiguration.getBuilder();
        builder.setSslConfiguration(sslConf);
        return builder;
    }
}


