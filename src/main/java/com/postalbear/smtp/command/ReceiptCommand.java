package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

/**
 * Implementation of RCPT command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public class ReceiptCommand extends BaseCommand {

    private static final String TO_KEY_WORD = "TO:";
    private final LineParser lineParser = new LineParser();

    public ReceiptCommand() {
        super("RCPT");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String smtpLine, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        try {
            String line = CommandUtils.removeCommandFromLine(smtpLine);
            validate(session, line);
            line = removeKeyword(line);
            handleInternal(session, lineParser.parseLine(line));
        } catch (SmtpException ex) {
            session.sendResponse(ex.getResponseCode(), ex.getResponseMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    private void validate(SmtpSession session, String line) {
        if (!StringUtils.startsWithIgnoreCase(line, TO_KEY_WORD)) {
            throw new SmtpException(501, "5.5.2 Syntax error: RCPT TO: <address>");
        }
        if (!session.isMailTransactionInProgress()) {
            throw new SmtpException(503, "5.5.1 Invalid sequence: need MAIL command fist");
        }
        int maxRecipients = session.getConfiguration().getMaxRecipients();
        if (maxRecipients > 0 && session.getRecipientsCount() >= maxRecipients) {
            throw new SmtpException(452, "4.5.3 Error: too many recipients");
        }
    }

    private String removeKeyword(String line) {
        line = line.substring(TO_KEY_WORD.length()).trim();
        return line;
    }

    private void handleInternal(SmtpSession session, ParsingResult result) throws IOException {
        try {
            // rest of line might contain parameters    
            handleParameters(result.getRestOfLine());

            InternetAddress address = new InternetAddress(result.getEmailAddress());
            session.recipient(address);

            session.sendResponse(250, "recipient <" + address.getAddress() + "> OK");
        } catch (AddressException ex) {
            session.sendResponse(553, "5.1.3 Syntax error: recipient address <" + result.getEmailAddress() + "> is invalid");
        }
    }

    /**
     * parse and handle parameters after email address.
     *
     * @param line
     */
    private void handleParameters(String line) {
        if (StringUtils.isNotBlank(line)) {
            throw new SmtpException(555, "Command parameter \"" + line + "\" not implemented");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-RCPT command, to specify recipient of e-mail, separate invocation for each recipient should be done.");
        session.sendResponseAsString("214-RCPT TO: <recipient> [ <parameters> ]");
        session.sendResponseAsString("214-Currently command does not support parameters");
    }
}
