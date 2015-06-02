package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.io.IOException;

/**
 * Implementation of DATA command.
 * See RFC5321
 *
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public class DataCommand extends BaseCommand {

    public DataCommand() {
        super("DATA");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String line, @NonNull SmtpSession session) throws IOException {
        if (!session.isMailTransactionInProgress()) {
            throw new SmtpException(503, "5.5.1 Invalid command sequence: need MAIL command");
        }
        if (session.getRecipientsCount() == 0) {
            throw new SmtpException(503, "5.5.1 Invalid command sequence: need RCPT command");
        }

        DataHandler handler = session.getConfiguration().getDataHandlerFactory().create(session);
        handler.kickstartData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-DATA command, to send content of e-mail to server.");
        session.sendResponseAsString("214-End data with <CR><LF>.<CR><LF>");
    }
}
