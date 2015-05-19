package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpProcessor;
import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.command.Command;
import com.postalbear.smtp.command.CommandRegistry;
import lombok.NonNull;

import java.io.IOException;

/**
 * This processor should parse SMTP commands from input and process them one by one.
 *
 * @author Grigory Fadeev
 */
public class PipeliningProcessor implements SmtpProcessor {

    private CommandRegistry commandRegistry;

    /**
     * Constructs PipeliningProcessor instance.
     *
     * @param commandRegistry with supported set of SMTP commands
     */
    public PipeliningProcessor(@NonNull CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, SmtpSession session) throws IOException {
        String smtpLine = smtpInput.getSmtpLine();
        Command command = commandRegistry.getCommand(smtpLine);
        command.handle(smtpLine, session, smtpInput);
    }
}
