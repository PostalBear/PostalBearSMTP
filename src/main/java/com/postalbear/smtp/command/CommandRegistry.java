package com.postalbear.smtp.command;

import com.postalbear.smtp.exception.SmtpException;

import java.util.Set;

/**
 * Implementations intended to match SMTP command from given SMTP line.
 *
 * @author Grigory Fadeev
 */
public interface CommandRegistry {

    /**
     * @param smtpLine to parse Command from
     * @return command responsible for handling given line
     */
    public Command getCommand(String smtpLine) throws SmtpException;

    /**
     * @return set of supported commands.
     */
    public Set<String> getSupportedCommands();
}
