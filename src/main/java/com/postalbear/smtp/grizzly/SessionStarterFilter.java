package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.ssl.SSLBaseFilter;
import org.glassfish.grizzly.utils.NullaryFunction;

import java.io.IOException;

import static org.glassfish.grizzly.attributes.AttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * Filter intended to start new SMTP session and show welcome banner.
 *
 * @author Grigory Fadeev
 */
public class SessionStarterFilter extends BaseFilter implements SSLBaseFilter.HandshakeListener {

    //Flag to cover STARTTLS case to do not send welcome banner twice
    public static final Attribute<Boolean> WELCOME_BANNER_SHOWN_ATTRIBUTE =
            DEFAULT_ATTRIBUTE_BUILDER.createAttribute("WelcomeBannerShown", (NullaryFunction<Boolean>) () -> false);

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
        Connection connection = ctx.getConnection();
        SmtpSession session = sessionProvider.startNewSession(ctx);
        /*
        * Show welcome banner only for plain SMTP connections,
        * for TLS connections banner should be shown only after handshake is done.
        */
        if (!session.isConnectionSecured()) {
            showServerBanner(connection, session);
        }
        return ctx.getStopAction();
    }

    private void showServerBanner(Connection connection, SmtpSession session) {
        String welcomeMessage = configuration.getHostName() + " ESMTP " + configuration.getSoftwareName();
        session.sendResponse(220, welcomeMessage);
        session.flush();
        markWelcomeBannerAsShown(connection);
    }

    @Override
    public void onComplete(Connection connection) {
        FilterChainContext ctx = createContext(connection, FilterChainContext.Operation.NONE);
        SmtpSession session = sessionProvider.startNewSession(ctx);
        //to avoid showing welcome banner twice in case of plain SMTP + STARTTLS
        if (!isWelcomeBannerShown(connection)) {
            showServerBanner(connection, session);
        }
    }

    private boolean isWelcomeBannerShown(Connection connection) {
        return WELCOME_BANNER_SHOWN_ATTRIBUTE.get(connection);
    }

    private void markWelcomeBannerAsShown(Connection connection) {
        WELCOME_BANNER_SHOWN_ATTRIBUTE.set(connection, true);
    }

    @Override
    public void onStart(Connection connection) {
        //do nothing on handshake start
    }
}