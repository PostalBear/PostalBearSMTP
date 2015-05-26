package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of STARTTLS command.
 *
 * @author Michael Wildpaner &lt;mike@wildpaner.com&gt;
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public class StartTLSCommand extends BaseCommand {

    public StartTLSCommand() {
        super("STARTTLS");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session) throws IOException {
        if (!CommandUtils.removeCommandFromLine(line).isEmpty()) {
            throw new SmtpException(501, "Syntax error (no parameters allowed)");
        }
        if (!session.getConfiguration().isStartTlsEnabled()) {
            throw new SmtpException(454, "TLS not supported");
        }
        if (session.isConnectionSecured()) {
            throw new SmtpException(454, "TLS not available due to temporary reason: TLS already active");
        }
        session.sendResponse(220, "Ready to start TLS");
        session.flush();
        session.startTls();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-STARTTLS command, to ask server for transport encryption.");
    }
}
