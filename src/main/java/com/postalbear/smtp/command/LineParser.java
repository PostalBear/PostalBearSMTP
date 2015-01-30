package com.postalbear.smtp.command;

import com.postalbear.smtp.exception.SmtpException;

/**
 * Parse e-mail address and rest of parameters from SMTP line.
 *
 * @author Grigory Fadeev
 */
public class LineParser {

    /**
     * Get email address and rest of parameters from command line.
     *
     * @param line to parse
     * @return parsing result
     * @throws SmtpException if syntax invalid
     */
    public ParsingResult parseLine(String line) throws SmtpException {
        int openingAngleIndex = line.indexOf('<');
        if (openingAngleIndex == 0) {
            return parseWithAngleBrackets(line);
        } else {
            return parseWithUnquotedSpace(line);
        }
    }

    /**
     * Parse line with following pattern "<address> [parameters]".
     *
     * @param line
     * @return
     */
    private ParsingResult parseWithAngleBrackets(String line) throws SmtpException {
        ParsingResult result = new ParsingResult();
        int closingAngleIndex = line.lastIndexOf('>');
        if (closingAngleIndex < 0) {
            throw new SmtpException(501, "5.5.2 Syntax error: closing '>' not found");
        }
        // spaces within the <> are also possible, Postfix apparently
        // trims these away:
        result.setEmailAddress(line.substring(1, closingAngleIndex).trim());
        if (line.length() > closingAngleIndex + 1) {
            result.setRestOfLine(line.substring(closingAngleIndex + 1).trim());
        }
        return result;
    }

    /**
     * Parse line with following pattern "address [parameters]".
     *
     * @param line
     * @return
     */
    private ParsingResult parseWithUnquotedSpace(String line) {
        ParsingResult result = new ParsingResult();
        //skip all double quoted address parts/ quoted spaces
        //see http://tools.ietf.org/html/rfc5321#section-4.1.2 for additional details
        int spaceIndex = findUnquotedSpaceIndex(line);
        result.setEmailAddress(line.substring(0, spaceIndex).trim());
        //
        int parametersIndex = spaceIndex + 1;
        if (line.length() > parametersIndex) {
            result.setRestOfLine(line.substring(parametersIndex).trim());
        }
        return result;
    }

    private int findUnquotedSpaceIndex(String address) {
        boolean isQuoted = false;
        for (int i = 0; i < address.length(); i++) {
            char symbol = address.charAt(i);
            if (symbol == '\\') {
                i++;//skip escaped character
            }
            if (symbol == '"') {
                isQuoted = !isQuoted;
            }
            if (symbol == ' ' && !isQuoted) {
                return i;
            }
        }
        return address.length();
    }
}
