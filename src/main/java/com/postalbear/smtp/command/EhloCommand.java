package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of EHLO command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public class EhloCommand extends BaseCommand {

    private static final String HYPHEN = "-";

    public EhloCommand() {
        super("EHLO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        String arg = CommandUtils.removeCommandFromLine(line);
        if (arg.isEmpty()) {
            throw new SmtpException(501, "5.5.2 Syntax error: EHLO hostname");
        }

        session.resetMailTransaction();
        session.helo(arg);

        session.sendResponseAsString(250 + HYPHEN + session.getConfiguration().getHostName());
        session.sendResponseAsString(250 + HYPHEN + "8BITMIME");

        if (session.getConfiguration().isStartTlsEnabled()) {
            session.sendResponseAsString(250 + HYPHEN + "STARTTLS");
        }

        int maxSize = session.getConfiguration().getMaxMessageSize();
        if (maxSize > 0) {
            session.sendResponseAsString(250 + HYPHEN + "SIZE " + maxSize);
        }

        AuthenticationHandlerFactory authFactory = session.getConfiguration().getAuthenticationFactory();
        if (authFactory != null && !authFactory.getAuthenticationMechanisms().isEmpty()) {
            String joinedMechanisms = String.join(" ", authFactory.getAuthenticationMechanisms());
            session.sendResponseAsString(250 + HYPHEN + AuthCommand.VERB + " " + joinedMechanisms);
        }

        session.sendResponse(250, "OK");
        // explicitly stated by RFC2920 3.2.5
        session.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-Used by client to introduce itself.");
        session.sendResponseAsString("214-EHLO <hostname>");
    }
}
