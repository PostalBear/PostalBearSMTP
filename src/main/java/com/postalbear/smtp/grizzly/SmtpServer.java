package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.ConfigurationProvider;
import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.command.CommandRegistry;
import org.apache.commons.lang3.Validate;
import org.glassfish.grizzly.filterchain.*;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.ssl.SSLFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Represents SMTP(S) server itself.
 *
 * @author Grigory Fadeev
 */
public class SmtpServer implements ConfigurationProvider {

    private final SmtpServerConfiguration configuration;
    private final TCPNIOTransport transport;
    //
    private SmtpSessionProvider smtpSessionProvider;
    // chain and filters
    private FilterChain defaultChain;
    private SSLFilter sslFilter;
    private SmtpResponseCodecFilter smtpResponseCodecFilter;
    private SessionStarterFilter sessionStarterFilter;
    private SmtpFilter smtpFilter;


    public SmtpServer(@Nonnull SmtpServerConfiguration configuration,
                      @Nonnull TCPNIOTransport transport) {
        this.configuration = configuration;
        this.transport = transport;
    }

    public void start(String host, int port) throws IOException {
        Validate.isTrue(transport.isStopped(), "Server is already started");
        initFilterChain();
        transport.bind(port);
        transport.start();
    }

    private void initFilterChain() {
        smtpSessionProvider = new SmtpSessionProvider(this);
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new ExceptionLoggingFilter());
        // Add TransportFilter, which is responsible
        // for reading and writing data to the connection
        filterChainBuilder.add(new TransportFilter());

        smtpResponseCodecFilter = new SmtpResponseCodecFilter();
        sessionStarterFilter = new SessionStarterFilter(smtpSessionProvider, getConfiguration());
        smtpFilter = new SmtpFilter(smtpSessionProvider, CommandRegistry.getCommandHandler());

        if (configuration.getSslConfig() != null) {
            sslFilter = new SSLFilter(configuration.getSslConfig(), null);
            if (configuration.isSmtpsEnabled()) {
                sslFilter.addHandshakeListener(sessionStarterFilter);
                filterChainBuilder.add(sslFilter);
            }
        }

        filterChainBuilder.add(smtpResponseCodecFilter);
        filterChainBuilder.add(sessionStarterFilter);
        filterChainBuilder.add(smtpFilter);

        defaultChain = filterChainBuilder.build();
        transport.setProcessor(defaultChain);
    }

    public void shutdown() throws IOException {
        transport.shutdownNow();
    }

    @Override
    public SmtpServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Check whether connection is plain SMTP connection or secured by TLS.
     * installed
     *
     * @param context
     * @return
     */
    boolean isConnectionSecured(FilterChainContext context) {
        for (Filter filter : context.getFilterChain()) {
            if (filter instanceof SSLFilter) {
                return true;
            }
        }
        return false;
    }

    /**
     * Install security layer to connection.
     */
    void installSslFilter(FilterChainContext context) {
        //filter chain has some internal dependency for filter index
        //if we just add new filter to current chain we will corrupt it.
        FilterChain securedFilterChain = new DefaultFilterChain(defaultChain);
        int transportFilterIndex = defaultChain.indexOfType(TransportFilter.class);
        // Add connection security layer to the chain
        securedFilterChain.add(transportFilterIndex + 1, sslFilter);
        context.getConnection().setProcessor(securedFilterChain);
    }
}
