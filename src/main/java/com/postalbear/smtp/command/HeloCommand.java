package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of HELO command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public class HeloCommand extends BaseCommand {

    public HeloCommand() {
        super("HELO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        String args = CommandUtils.removeCommandFromLine(line);
        if (args.isEmpty()) {
            throw new SmtpException(501, "5.5.2 Syntax error: HELO <hostname>");
        }

        session.resetMailTransaction();
        session.helo(args);
        session.sendResponse(250, session.getConfiguration().getHostName());
        // explicitly stated by RFC2920 3.2.5
        session.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-Used by client to introduce itself.");
        session.sendResponseAsString("214-HELO <hostname>");
    }
}
