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
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    protected InternetAddress sender;
    protected InternetAddress recipient;

    private CapturingHandler transactionHandler;
    private SmtpServer server;
    private String localhost;
    private int port;

    protected static String concatStrings(String... lines) {
        return String.join(SmtpConstants.CRLF, lines);
    }

    @Before
    public final void baseInit() throws Exception {
        port = findFreePort();
        localhost = new InetSocketAddress(port).getHostName();
        sender = new InternetAddress("envelope_sender@domain.com");
        recipient = new InternetAddress("envelope_recipient@domain.com");
        transactionHandler = new CapturingHandler();
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
        Transport transport = session.getTransport();
        try {
            transport.connect();
            transport.sendMessage(message, addresses);
            assertTrue(transport.isConnected());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }

    protected void assertMessageReceived() {
        assertEquals(CLIENT_HELO, transactionHandler.getClientHelo());
        assertEquals(sender, transactionHandler.getSender());
        assertTrue(transactionHandler.getRecipients().size() == 1);
        assertTrue(transactionHandler.getRecipients().contains(recipient));
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

        return builder.build();
    }

    private int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    class CapturingHandler implements SmtpTransactionHandler {

        private String clientHelo;
        private InternetAddress sender;
        private int messageSize;
        private List<InternetAddress> recipients = new ArrayList<>();
        private String messageContent;

        @Override
        public void helo(String clientHelo) {
            this.clientHelo = clientHelo;
        }

        @Override
        public void messageSize(int size) {
            messageSize = size;
        }

        @Override
        public void from(InternetAddress from) {
            sender = from;
        }

        @Override
        public void recipient(InternetAddress recipient) {
            recipients.add(recipient);
        }

        @Override
        public void data(InputStream data) throws IOException {
            messageContent = IOUtils.toString(data);
        }

        public String getMessageAsString() {
            return messageContent;
        }

        public String getClientHelo() {
            return clientHelo;
        }

        public InternetAddress getSender() {
            return sender;
        }

        public int getMessageSize() {
            return messageSize;
        }

        public List<InternetAddress> getRecipients() {
            return recipients;
        }
    }

    private class TestHandlerFactory implements SmtpTransactionHandlerFactory {

        @Override
        public SmtpTransactionHandler getHandler() {
            return transactionHandler;
        }
    }
}
