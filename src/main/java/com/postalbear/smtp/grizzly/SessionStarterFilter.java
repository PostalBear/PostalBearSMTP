package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpServerConfiguration;
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

    private final SmtpServerConfiguration configuration;
    private final SmtpSessionProvider sessionProvider;

    /**
     * Constructs instance of SessionStarterFilter.
     *
     * @param sessionProvider to start new SMTP session
     * @param configuration   current configuration.
     */
    public SessionStarterFilter(SmtpSessionProvider sessionProvider, SmtpServerConfiguration configuration) {
        this.configuration = configuration;
        this.sessionProvider = sessionProvider;
    }

    @Override
    public NextAction handleAccept(FilterChainContext ctx) throws IOException {
        GrizzlySmtpSession session = sessionProvider.startNewSession(ctx);
        if (isSmtpCase(session)) {
            showServerBanner(session);
        }
        return ctx.getStopAction();
    }

    private boolean isSmtpCase(GrizzlySmtpSession session) {
        return !session.isConnectionSecured();
    }

    /**
     * Method will be invoked when TLS handshake is complete.
     * There are two possible reasons for initiating secured connection:
     * 1. STARTTLS was requested by client (for this case we need to reset server state as requested by RFC3207).
     * 2. This is SMTPS connection from begining.
     *
     * @param connection
     */
    @Override
    public void onComplete(Connection connection) {
        FilterChainContext ctx = createContext(connection, FilterChainContext.Operation.NONE);
        GrizzlySmtpSession session = sessionProvider.getSmtpSession(ctx);
        if (!session.isWelcomeBannerShown()) {
            showServerBanner(session);
        } else {
            sessionProvider.startNewSession(ctx).markWelcomeBannerAsShown();
        }
    }

    private void showServerBanner(GrizzlySmtpSession session) {
        String welcomeMessage = configuration.getHostName() + " ESMTP " + configuration.getSoftwareName();
        session.sendResponse(220, welcomeMessage);
        session.flush();
        session.markWelcomeBannerAsShown();
    }

    @Override
    public void onStart(Connection connection) {
        //do nothing on handshake start
    }
}