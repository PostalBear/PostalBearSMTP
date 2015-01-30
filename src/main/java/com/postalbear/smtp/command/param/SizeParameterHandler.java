package com.postalbear.smtp.command.param;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;

/**
 * Handler for SIZE parameter.
 * Example "SIZE=5000"
 * See RFC1870 <br/>
 *
 * @author Grigory Fadeev
 */
public class SizeParameterHandler {

    private static final String PARAMETER_NAME = "size";
    private static final String PARAMETER_KEY = PARAMETER_NAME + "=";

    /**
     * Checks whether given line matches parameter pattern.
     *
     * @param line to check
     * @return true if matches
     */
    public boolean match(String line) {
        return !line.isEmpty() && line.startsWith(PARAMETER_NAME);
    }

    /**
     * Process given line accordingly.
     *
     * @param session current SMTP session
     * @param line    to process
     */
    public void handleLine(SmtpSession session, String line) {
        checkSyntax(line);
        try {
            int size = Integer.parseInt(getValueStr(line));
            int maxSize = session.getConfiguration().getMaxMessageSize();
            if (maxSize > 0 && size > maxSize) {
                throw new SmtpException(552, "5.3.4 Message size exceeds fixed limit");
            }
            session.messageSize(size);
        } catch (NumberFormatException ex) {
            throw new SmtpException(501, "failed to get value of SIZE parameter");
        }
    }

    /**
     * Checks that line contains not only parameter name but also a value.
     *
     * @param line
     */
    private void checkSyntax(String line) {
        if (PARAMETER_KEY.length() > line.length()) {
            throw new SmtpException(501, "incorrect syntax of SIZE parameter");
        }
    }

    /**
     * Removes parameter name and = symbol from line.
     *
     * @param line
     * @return
     */
    private String getValueStr(String line) {
        return line.substring(PARAMETER_KEY.length());
    }
}
