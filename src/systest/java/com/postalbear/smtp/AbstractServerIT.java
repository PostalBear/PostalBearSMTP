package com.postalbear.smtp;

import com.postalbear.smtp.grizzly.SmtpServer;
import com.sun.mail.smtp.SMTPMessage;
import org.apache.commons.io.IOUtils;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractServerIT {

    public static final String CLIENT_HELO = "PostalBear_Test_Client";
    public static final String MESSAGE_CONTENT = concatStrings(
            "Message-ID: <1586845078.0.1417271716872@ITestRunner>",
            "To: recipient@domain.com",
            "From: sender@domain.com",
            "Subject: Hello PostalBear",
            "MIME-Version: 1.0",
            "Content-Type: text/plain; charset=us-ascii",
            "Content-Transfer-Encoding: 7bit",
            "",
            "Hello from integration test !",
            "");

    //Be aware that this is a partial mock
    @Mock
    protected CapturingHandler transactionHandler;

    protected InternetAddress sender;
    protected InternetAddress recipient;

    private SmtpServer server;
    private String localhost;
    private int port;

    protected static String concatStrings(String... lines) {
        return String.join(IOUtils.LINE_SEPARATOR, lines);
    }

    @Before
    public final void baseInit() throws Exception {
        port = findFreePort();
        localhost = new InetSocketAddress(port).getHostName();
        sender = new InternetAddress("envelope_sender@domain.com");
        recipient = new InternetAddress("envelope_recipient@domain.com");
        /**
         * I've decided to use partial mocking since for Data command instance of
         * SmtpTransactionHandler should consume full message content otherwise
         * rest will be interpreted as following SMTP commands (Pipelining) and lead to errors.
         */
        doCallRealMethod().when(transactionHandler).data(any(InputStream.class));
        when(transactionHandler.getMessageAsString()).thenCallRealMethod();
    }

    @After
    public final void baseCleanup() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    protected Properties getSessionProperties(String protocol) {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", protocol);
        properties.setProperty("mail." + protocol + ".host", localhost);
        properties.setProperty("mail." + protocol + ".port", String.valueOf(port));
        properties.setProperty("mail." + protocol + ".localhost", CLIENT_HELO);

        properties.setProperty("mail." + protocol + ".quitwait", "true");
        return properties;
    }

    protected void sendMessage(Session session, SMTPMessage message, InternetAddress... addresses) throws Exception {
        session.setDebug(true);
        session.setDebugOut(System.out);
        Transport transport = session.getTransport();
        try {
            transport.connect();
            transport.sendMessage(message, addresses);
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }

    protected void assertMessageReceived() {
        Mockito.verify(transactionHandler).helo(eq(CLIENT_HELO));
        Mockito.verify(transactionHandler).from(eq(sender));
        Mockito.verify(transactionHandler).recipient(eq(recipient));
        assertEquals(MESSAGE_CONTENT, transactionHandler.getMessageAsString());
    }

    protected void createServer(SmtpServerConfiguration.Builder builder) throws Exception {
        builder.setHostName(localhost).
                setSoftwareName("GrizzlySMTP").
                setHandlerFactory(new TestHandlerFactory());

        server = new SmtpServer(builder.buildConfiguration(), createTcpNioTrasnport());
        server.start(localhost, port);
    }

    private TCPNIOTransport createTcpNioTrasnport() throws Exception {
        //just to do not have tons of threads i limited it to 1
        ThreadPoolConfig poolConfig = ThreadPoolConfig.defaultConfig().
                setCorePoolSize(1).
                setMaxPoolSize(1);
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance()
                .setServerConnectionBackLog(1)
                .setSelectorRunnersCount(1)
                .setSelectorThreadPoolConfig(poolConfig)
                .setWorkerThreadPoolConfig(poolConfig)
                .setReadBufferSize(2 * 1024);//2KB read buffer

        TCPNIOTransport transport = builder.build();
        return transport;
    }

    private int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Partially implements SmtpTransactionHandler, rest is mocked by Mockito library.
     * I decided to use partial mocking since:
     * 1. handler should consume complete mail content to prevent errors, non consumed content interpreted as followup SMTP commands.
     * 2. it's not convenient to consume mail content just with plain Mockito API.
     */
    abstract class CapturingHandler implements SmtpTransactionHandler {

        private String messageContent;

        @Override
        public void data(InputStream data) throws IOException {
            messageContent = IOUtils.toString(data);
        }

        public String getMessageAsString() {
            return messageContent;
        }
    }

    private class TestHandlerFactory implements SmtpTransactionHandlerFactory {

        @Override
        public SmtpTransactionHandler getHandler() {
            return transactionHandler;
        }
    }
}
