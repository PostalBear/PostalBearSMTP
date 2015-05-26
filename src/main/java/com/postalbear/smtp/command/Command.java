package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpSession;

import java.io.IOException;

/**
 * Interface of SMTP Command handler.
 *
 * @author Jon Stevens
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public interface Command {

    /**
     * Handle given SMTP line.
     *
     * @param line    to process
     * @param session current SMTP session
     * @throws java.io.IOException
     */
    void handle(String line, SmtpSession session) throws IOException;

    /**
     * Sends help response back to client.
     *
     * @param session to send help response back to client
     * @throws IOException
     */
    void printHelpMessage(SmtpSession session) throws IOException;

    /**
     * Get command name.
     *
     * @return command name
     */
    String getName();
}
