/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Verifies the presence of a TLS connection if TLS is required.
 * The wrapped command is executed when the check succeeds.
 *
 * @author Evgeniy Naumenko
 * @author Grigory Fadeev
 */
public class RequireAuthCommandWrapper implements Command {

    private final Command command;

    public RequireAuthCommandWrapper(Command command) {
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
    public void handle(@NonNull String line, @NonNull SmtpSession session) throws IOException {
        if (!session.isAuthenticated() && session.getConfiguration().isAuthenticationEnforced()) {
            throw new SmtpException(530, "5.7.0 Authentication required");
        }
        command.handle(line, session);
    }

    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        command.printHelpMessage(session);
    }
}
