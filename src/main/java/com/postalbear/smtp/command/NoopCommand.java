package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of NOOP command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff SchnitzerА
 * @author Grigory Fadeev
 */
public class NoopCommand extends BaseCommand {

    public NoopCommand() {
        super("NOOP");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session) throws IOException {
        session.sendResponse(250, "OK");
        // explicitly stated by RFC2920 3.2.5
        session.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-NOOP command, simply do nothing.");
    }
}
