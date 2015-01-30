/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Verifies the presence of a TLS connection if TLS is required.
 * The wrapped command is executed when the test succeeds.
 * <p>
 * see RFC3207
 *
 * @author Erik van Oosten
 * @author Grigory Fadeev
 */
public class RequireTLSCommandWrapper implements Command {

    private final Command command;

    public RequireTLSCommandWrapper(Command command) {
        this.command = command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return command.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        if (session.getConfiguration().isStartTlsEnforced() && !session.isConnectionSecured()) {
            throw new SmtpException(530, "5.7.0 Must issue a STARTTLS command first");
        }
        command.handle(line, session, input);
    }

    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        command.printHelpMessage(session);
    }
}
