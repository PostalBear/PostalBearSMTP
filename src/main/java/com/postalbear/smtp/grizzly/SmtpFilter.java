package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.command.Command;
import com.postalbear.smtp.command.CommandHandler;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import java.io.IOException;

import static com.postalbear.smtp.grizzly.SmtpInputBuffer.getSmtpInputBuffer;

/**
 * Filter intended to parse and process SMTP commands received from client.
 *
 * @author Grigory Fadeev
 */
public class SmtpFilter extends BaseFilter {

    private final SmtpSessionProvider sessionProvider;
    private final CommandHandler commandHandler;

    public SmtpFilter(@NonNull SmtpSessionProvider sessionProvider, @NonNull CommandHandler commandHandler) {
        this.sessionProvider = sessionProvider;
        this.commandHandler = commandHandler;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        SmtpSession session = sessionProvider.getSmtpSession(ctx);
        SmtpInputBuffer smtpInput = getSmtpInputBuffer(ctx);
        smtpInput.appendDataChunk(ctx.getMessage());
        try {
            processInput(session, smtpInput);
        } catch (RuntimeException ex) {
            session.sendResponse(421, "4.3.0 Mail system failure, closing transmission channel");
            session.closeSession();
        }
        return ctx.getStopAction();
    }

    public void processInput(SmtpSession session, SmtpInputBuffer smtpInput) throws IOException {
        try {
            while (smtpInput.hasNextSmtpLine()) {
                String smtpLine = smtpInput.getSmtpLine();
                Command command = commandHandler.getCommand(smtpLine);
                command.handle(smtpLine, session, smtpInput);
            }
            if (smtpInput.isEmpty()) {
                smtpInput.release();
                session.flush();
            }
        } catch (SmtpException ex) {
            session.sendResponse(ex.getResponseCode(), ex.getResponseMessage());
            session.flush();
        }
    }
}
