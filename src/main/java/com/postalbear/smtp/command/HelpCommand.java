/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Implementation of HELP command.
 *
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public class HelpCommand extends BaseCommand {

    private static final String VERB = "HELP";
    private final CommandRegistry handler;

    public HelpCommand(@NonNull CommandRegistry handler) {
        super(VERB);
        this.handler = handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull String smtpLine, @NonNull SmtpSession session, @NonNull SmtpInput input) throws IOException {
        String topic = StringUtils.defaultIfBlank(CommandUtils.removeCommandFromLine(smtpLine), VERB);
        try {
            handler.getCommand(topic).printHelpMessage(session);
            session.sendResponseAsString("214 End of " + topic + " info");
            session.flush();
        } catch (SmtpException ex) {
            session.sendResponse(504, "Topic " + topic + "is unknown");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printHelpMessage(SmtpSession session) throws IOException {
        session.sendResponseAsString("214-The HELP command gives help info about the topic specified.");
        SmtpServerConfiguration configuration = session.getConfiguration();
        session.sendResponseAsString("214-Server inof: " + configuration.getSoftwareName() + " on " + configuration.getHostName());
        session.sendResponseAsString("214-Topics:");
        for (String command : handler.getSupportedCommands()) {
            session.sendResponseAsString("214-  " + command);
        }
        session.sendResponseAsString("214-For more info use \"HELP <topic>\"");
    }
}
