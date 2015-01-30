package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpServerConfiguration;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.ssl.SSLBaseFilter;

import java.io.IOException;

import static com.postalbear.smtp.grizzly.GrizzlySmtpSession.getSmtpSession;

/**
 * Filter intended to show SMTP welcome banner for connected clients.
 *
 * @author Grigory Fadeev
 */
public class WelcomeBannerFilter extends BaseFilter implements SSLBaseFilter.HandshakeListener {

    private final SmtpServer server;
    private final SmtpServerConfiguration configuration;

    public WelcomeBannerFilter(SmtpServer server) {
        this.server = server;
        this.configuration = server.getConfiguration();
    }

    @Override
    public NextAction handleAccept(FilterChainContext ctx) throws IOException {
        /*
        * Show welcome banner only for plain SMTP connections.
        * For TLS connections banner should be shown only after handshake is done,
        * otherwise will be broken cause client expects ServerHello and not greetings from server...
        */
        if (!server.isConnectionSecured(ctx)) {
            showServerBanner(getSmtpSession(server, ctx));
        }
        return ctx.getStopAction();
    }

    @Override
    public void onComplete(Connection connection) {
        FilterChainContext ctx = createContext(connection, FilterChainContext.Operation.NONE);
        showServerBanner(getSmtpSession(server, ctx));
    }

    private void showServerBanner(GrizzlySmtpSession session) {
        String welcomeMessage = configuration.getHostName() + " ESMTP " + configuration.getSoftwareName();
        session.sendResponse(220, welcomeMessage);
        session.flush();
    }

    @Override
    public void onStart(Connection connection) {
        //do nothing on handshake start
    }
}
