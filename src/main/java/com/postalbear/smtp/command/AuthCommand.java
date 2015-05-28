package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of AUTH command.
 * See RFC4954
 *
 * @author Marco Trevisan
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public class AuthCommand extends BaseCommand {

    public static final String VERB = "AUTH";

    public AuthCommand() {
        super(VERB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session) throws IOException {
        if (session.isAuthenticated()) {
            throw new SmtpException(503, "Refusing any other AUTH command.");
        }
        AuthenticationHandlerFactory authFactory = session.getConfiguration().getAuthenticationFactory();
        if (authFactory == null) {
            throw new SmtpException(502, "Authentication not supported");
        }

        List<String> arguments = CommandUtils.getArguments(line);
        if (arguments.size() < 2) {
            throw new SmtpException(501, "Syntax error: " + getName() + " mechanism [initial-response]");
        }
        String mechanism = arguments.get(1).toUpperCase(Locale.ENGLISH);
        AuthenticationHandler handler = authFactory.create(mechanism, session);
        handler.kickstartAuth(line);
        session.setAuthenticationHandler(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-Service for authentication procedure.");
        session.sendResponseAsString("214-AUTH <mechanism> [initial-response]");
        session.sendResponseAsString("214-  mechanism = SASL authentication mechanism to use,");
        session.sendResponseAsString("214-  initial-response = an optional base64-encoded response");
    }
}
