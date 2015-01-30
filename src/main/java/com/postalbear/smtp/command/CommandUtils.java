/*
 */
package com.postalbear.smtp.command;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper class containing few useful methods for dealing with SMTP lines.
 *
 * @author Grigory Fadeev <grigory.fadeev@gmail.com>
 */
public final class CommandUtils {

    private CommandUtils() {
    }

    /**
     * Get SMTP line without command.
     *
     * @param commandString
     * @return SMTP line without command
     */
    public static String removeCommandFromLine(String commandString) {
        return StringUtils.substringAfter(commandString, StringUtils.SPACE);
    }

    /**
     * Get arguments from SMTP line.
     *
     * @param smtpLine to parse
     * @return
     */
    public static List<String> getArguments(String smtpLine) {
        StringTokenizer tokenizer = new StringTokenizer(smtpLine);
        List<String> result = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }
}
