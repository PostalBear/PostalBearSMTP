package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.AbstractSmtpSession;
import lombok.NonNull;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainContext;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

import static org.glassfish.grizzly.attributes.AttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * Implementation of AbstractSmtpSession, use Grizzly framework as a transport.
 * I doubt that somewhen transport layer will be changed, but such separation makes code a little bit easier to read.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class GrizzlySmtpSession extends AbstractSmtpSession {

    public static final Attribute<GrizzlySmtpSession> SMTP_SESSION = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpSession");

    private final List<String> responseBuffer = new ArrayList<>();
    private final SmtpServer server;
    private FilterChainContext context;

    public GrizzlySmtpSession(SmtpServer server) {
        super(server);
        this.server = server;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void sendResponse(int code, @NonNull String message) {
        responseBuffer.add(code + " " + message);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void sendResponseAsString(String message) {
        responseBuffer.add(message);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void closeSession() {
        flush();
        context.getConnection().closeSilently();
    }

    @Override
    public void flush() {
        if (!responseBuffer.isEmpty()) {
            context.write(responseBuffer);
            responseBuffer.clear();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isConnectionSecured() {
        return server.isConnectionSecured(context);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void startTls() {
        server.installSslFilter(context);
    }

    public static GrizzlySmtpSession getSmtpSession(SmtpServer server, FilterChainContext ctx) {
        GrizzlySmtpSession session = SMTP_SESSION.get(ctx.getConnection());
        if (session == null) {
            session = new GrizzlySmtpSession(server);
        }
        session.context = ctx;
        //associate session with connection
        SMTP_SESSION.set(ctx.getConnection(), session);
        return session;
    }
}
