package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of QUIT command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public class QuitCommand extends BaseCommand {

    public QuitCommand() {
        super("QUIT");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        session.sendResponse(221, session.getConfiguration().getHostName() + " Bye");
        session.closeSession();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-QUIT command, to close current SMTP session.");
    }
}
