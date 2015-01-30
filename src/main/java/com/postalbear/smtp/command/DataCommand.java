package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.io.DotTerminatedInputStream;
import com.postalbear.smtp.io.DotUnstuffingInputStream;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;

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
    public void handle(@NonNull String line, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        if (!session.isMailTransactionInProgress()) {
            throw new SmtpException(503, "5.5.1 Invalid command sequence: need MAIL command");
        }
        if (session.getRecipientsCount() == 0) {
            throw new SmtpException(503, "5.5.1 Invalid command sequence: need RCPT command");
        }
        session.sendResponse(354, "End data with <CR><LF>.<CR><LF>");
        session.flush();
        InputStream dotTerminatedStream = new DotTerminatedInputStream(input.getInputStream());
        try {
            session.data(new DotUnstuffingInputStream(dotTerminatedStream));
            session.sendResponse(250, "OK");
            session.flush();
            session.resetMailTransaction();
        } finally {
            //Important for Pipelining !
            //If for some reasons client did not consume all content of the message it will be done by server.
            //Essential since otherwise rest of data will be interpreted as followup SMTP commands.
            IOUtils.copyLarge(dotTerminatedStream, NullOutputStream.NULL_OUTPUT_STREAM);
            //Original stream is backed by SmtpInputBuffer so nothing to close actually, but ...
            IOUtils.closeQuietly(dotTerminatedStream);
        }
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
