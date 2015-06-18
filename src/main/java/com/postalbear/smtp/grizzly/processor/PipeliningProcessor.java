package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.command.Command;
import com.postalbear.smtp.command.CommandRegistry;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.SmtpLineDecoder;
import lombok.NonNull;

import java.io.IOException;

/**
 * Reads available SMTP lines and invokes corresponding command for further processing.
 *
 * @author Grigory Fadeev
 */
public class PipeliningProcessor implements SmtpProcessor {

    private final SmtpLineDecoder lineDecoder = SmtpLineDecoder.getInstance();
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
    public void process(SmtpInput smtpInput, GrizzlySmtpSession session) throws IOException {
        while (smtpInput.hasEnoughData(lineDecoder)) {
            String smtpLine = smtpInput.getData(lineDecoder);
            Command command = commandRegistry.getCommand(smtpLine);
            command.handle(smtpLine, session);
        }
    }
}
