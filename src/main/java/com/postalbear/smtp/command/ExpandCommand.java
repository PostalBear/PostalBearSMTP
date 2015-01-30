/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of EXPN command.
 *
 * @author Michele Zuccala < zuccala.m@gmail.com >
 * @author Grigory Fadeev
 */
public class ExpandCommand extends BaseCommand {

    public ExpandCommand() {
        super("EXPN");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        throw new SmtpException(502, "EXPN command is not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-EXPN command is not supported.");
    }
}
