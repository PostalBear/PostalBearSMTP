package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of VRFY command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Grigory Fadeev
 */
public class VerifyCommand extends BaseCommand {

    public VerifyCommand() {
        super("VRFY");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        throw new SmtpException(502, "VRFY command is disabled");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-VRFY command is not supported.");
    }
}
