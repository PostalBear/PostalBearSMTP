package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpSession;
import lombok.NonNull;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainContext;

import static org.glassfish.grizzly.attributes.AttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * Provides instance of GrizzlySmtpSession by demand.
 *
 * @author Grigory Fadeev
 */
public class SmtpSessionProvider {

    private static final Attribute<GrizzlySmtpSession> SMTP_SESSION_ATTRIBUTE =
            DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpSession");

    private final SmtpServer server;

    public SmtpSessionProvider(@NonNull SmtpServer server) {
        this.server = server;
    }

    /**
     * Creates new instance of GrizzlySmtpSession and associate it with current Connection.
     *
     * @param ctx to be able to send/receive data from client
     * @return instance of session
     */
    public SmtpSession startNewSession(FilterChainContext ctx) {
        GrizzlySmtpSession session = new GrizzlySmtpSession(server);
        session.refreshContext(ctx);
        //associate session with connection
        SMTP_SESSION_ATTRIBUTE.set(ctx.getConnection(), session);
        return session;
    }

    /**
     * Return instance of GrizzlySmtpSession associated with current Connection.
     *
     * @param ctx to get current Connection from
     * @return instance of session
     */
    public SmtpSession getSmtpSession(FilterChainContext ctx) {
        GrizzlySmtpSession session = SMTP_SESSION_ATTRIBUTE.get(ctx.getConnection());
        if (session == null) {
            throw new IllegalStateException("No SMTP session is associated with given connection");
        }
        session.refreshContext(ctx);
        return session;
    }
}
