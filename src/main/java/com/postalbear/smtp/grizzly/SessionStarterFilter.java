package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.ssl.SSLBaseFilter;

import java.io.IOException;

/**
 * Filter intended to start new SMTP session and show welcome banner.
 * <p>
 * Show welcome banner only for plain SMTP connections,
 * for TLS connections banner should be shown only after handshake is done, otherwise will brake handshake.
 *
 * @author Grigory Fadeev
 */
public class SessionStarterFilter extends BaseFilter implements SSLBaseFilter.HandshakeListener {

    private final SmtpSessionProvider sessionProvider;

    /**
     * Constructs instance of SessionStarterFilter.
     *
     * @param sessionProvider to start new SMTP session
     */
    public SessionStarterFilter(SmtpSessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    @Override
    public NextAction handleAccept(FilterChainContext ctx) throws IOException {
        SmtpSession session = sessionProvider.startNewSession(ctx);
        if (!session.isConnectionSecured()) {
            showServerBanner(session);
        }
        return ctx.getStopAction();
    }

    /**
     * Method is invoked when TLS handshake is complete.
     * There are two possible reasons for initiating secured connection:
     * 1. STARTTLS was requested by client (for this case we need to reset server state as requested by RFC3207).
     * 2. This is SMTPS connection from beginning and we need to show welcome banner.
     *
     * @param connection
     */
    @Override
    public void onComplete(Connection connection) {
        FilterChainContext ctx = createContext(connection, FilterChainContext.Operation.NONE);
        GrizzlySmtpSession session = sessionProvider.getSmtpSession(ctx);
        if (!session.isClientHeloDone()) {
            showServerBanner(session);
        } else {
            sessionProvider.startNewSession(ctx);
        }
    }

    private void showServerBanner(SmtpSession session) {
        SmtpServerConfiguration configuration = session.getConfiguration();
        String welcomeMessage = configuration.getHostName() + " ESMTP " + configuration.getSoftwareName();
        session.sendResponse(220, welcomeMessage);
        session.flush();
    }

    @Override
    public void onStart(Connection connection) {
        //nothing to do on handshake start
    }
}