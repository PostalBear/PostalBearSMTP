package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of RSET command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public class ResetCommand extends BaseCommand {

    public ResetCommand() {
        super("RSET");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        session.resetMailTransaction();
        session.sendResponse(250, "OK");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-RSET command, to reset current SMTP session state.");
    }
}
