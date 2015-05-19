package com.postalbear.smtp.command;

import com.postalbear.smtp.command.param.SizeParameterHandler;
import com.postalbear.smtp.exception.SmtpException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.postalbear.smtp.SmtpConstants.COMMAND_SEPARATOR;

/**
 * Contains all supported Commands.
 *
 * @author Grigory Fadeev
 */
public class CommandRegistryFactory {

    public static CommandRegistry create() {
        final CommandRegistryImpl handler = new CommandRegistryImpl();
        //
        handler.registerCommand(new AuthCommand(), true, false);
        handler.registerCommand(new DataCommand(), true, true);
        handler.registerCommand(new EhloCommand(), false, false);
        handler.registerCommand(new HeloCommand(), true, false);
        handler.registerCommand(new MailCommand(new SizeParameterHandler()), true, true);
        handler.registerCommand(new NoopCommand(), false, false);
        handler.registerCommand(new QuitCommand(), false, false);
        handler.registerCommand(new ReceiptCommand(), true, true);
        handler.registerCommand(new ResetCommand(), true, false);
        handler.registerCommand(new StartTLSCommand(), false, false);
        handler.registerCommand(new VerifyCommand(), true, true);
        handler.registerCommand(new ExpandCommand(), true, true);
        //
        //RFC 4954 explicitly required authentication (if enforced) even for HELP
        handler.registerCommand(new HelpCommand(handler), true, true);
        //
        return handler;
    }

    private static class CommandRegistryImpl implements CommandRegistry {

        private final Map<String, Command> commandMap = new HashMap<>();

        /**
         * Add command to register.
         *
         * @param command          to add
         * @param checkForStartTLS whether to enforce STARTTLS
         * @param checkForAuth     whether to enforce AUTH
         */
        void registerCommand(Command command, boolean checkForStartTLS, boolean checkForAuth) {
            String name = command.getName().toUpperCase(Locale.ENGLISH);
            Command tmp = command;
            if (checkForAuth) {
                tmp = new RequireAuthCommandWrapper(tmp);
            }
            if (checkForStartTLS) {
                tmp = new RequireTLSCommandWrapper(tmp);
            }
            commandMap.put(name, tmp);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Command getCommand(String smtpLine) throws SmtpException {
            String commandName = parseCommandBySpaceTerminator(smtpLine);
            Command command = commandMap.get(commandName);
            if (command == null) {
                throw new SmtpException(500, "SMTP command: \"" + commandName + "\" not implemented");
            }
            return command;
        }

        /**
         * Try to parse SMTP command from given string.
         *
         * @param smtpLine SMTP line
         * @return parsed command
         */
        private String parseCommandBySpaceTerminator(String smtpLine) {
            return StringUtils.substringBefore(smtpLine, COMMAND_SEPARATOR).toUpperCase(Locale.ENGLISH);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<String> getSupportedCommands() {
            return commandMap.keySet();
        }
    }
}
