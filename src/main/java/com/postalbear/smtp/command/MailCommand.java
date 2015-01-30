package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.command.param.SizeParameterHandler;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Implementation of MAIL command.
 * See RFC5321
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Scott Hernandez
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public class MailCommand extends BaseCommand {

    private static final String FROM_KEY_WORD = "FROM:";

    private final LineParser lineParser = new LineParser();
    private final SizeParameterHandler sizeHandler;

    public MailCommand(@NonNull SizeParameterHandler sizeHandler) {
        super("MAIL");
        this.sizeHandler = sizeHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String smtpLine, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        try {
            String line = CommandUtils.removeCommandFromLine(smtpLine);
            validate(session, line);
            line = removeKeyWord(line);
            handleInternal(session, lineParser.parseLine(line));
        } catch (SmtpException ex) {
            session.sendResponse(ex.getResponseCode(), ex.getResponseMessage());
        }
    }

    /**
     * Check given line for valid syntax.
     *
     * @param session
     * @param line
     */
    private void validate(SmtpSession session, String line) {
        if (!StringUtils.startsWithIgnoreCase(line, FROM_KEY_WORD)) {
            throw new SmtpException(501, "5.5.2 Syntax error: MAIL FROM: <address> [parameters]");
        }
        if (session.isMailTransactionInProgress()) {
            throw new SmtpException(503, "5.5.1 Invalid sequence: sender already specified.");
        }
    }

    private String removeKeyWord(String line) {
        return line.substring(FROM_KEY_WORD.length()).trim();
    }

    private void handleInternal(SmtpSession session, ParsingResult result) throws IOException {
        session.startMailTransaction();
        try {
            handleParameters(session, result.getRestOfLine());
            //
            InternetAddress address = new InternetAddress(result.getEmailAddress());
            session.from(address);
            session.sendResponse(250, "sender <" + address.getAddress() + "> OK");
        } catch (AddressException ex) {
            session.sendResponse(553, "5.1.7 Syntax error: sender address <" + result.getEmailAddress() + "> is invalid");
        }
    }

    private void handleParameters(SmtpSession session, String line) {
        StringTokenizer tokenizer = new StringTokenizer(line.toLowerCase(Locale.ENGLISH));
        while (tokenizer.hasMoreTokens()) {
            String parameter = tokenizer.nextToken();
            if (sizeHandler.match(parameter)) {
                sizeHandler.handleLine(session, parameter);
            } else {
                throw new SmtpException(504, "5.5.4 Command parameter \"" + line + "\" not implemented");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-MAIL command, to specify sender of e-mail.");
        session.sendResponseAsString("214-MAIL FROM: <sender> [ <parameters> ]");
        session.sendResponseAsString("214-List of supported parameters:");
        session.sendResponseAsString("214-  SIZE=to inform server about size of e-mail (in bytes)");
    }
}
