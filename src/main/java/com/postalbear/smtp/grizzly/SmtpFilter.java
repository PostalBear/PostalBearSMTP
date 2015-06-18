package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.command.CommandRegistry;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.processor.AuthenticationProcessor;
import com.postalbear.smtp.grizzly.processor.MessageReceivingProcessor;
import com.postalbear.smtp.grizzly.processor.PipeliningProcessor;
import com.postalbear.smtp.grizzly.processor.SmtpProcessor;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import java.io.IOException;

import static com.postalbear.smtp.grizzly.SmtpInputBuffer.getSmtpInputBuffer;

/**
 * Filter to process requests received from client.
 *
 * @author Grigory Fadeev
 */
public class SmtpFilter extends BaseFilter {

    private final SmtpSessionProvider sessionProvider;
    private final SmtpProcessor processingChain;

    /**
     * Constructs instance of SmtpFilter class.
     *
     * @param sessionProvider to obtain Session object
     * @param commandRegistry with supported SMTP commands
     */
    public SmtpFilter(SmtpSessionProvider sessionProvider, CommandRegistry commandRegistry) {
        this.sessionProvider = sessionProvider;
        processingChain = new AuthenticationProcessor(new MessageReceivingProcessor(new PipeliningProcessor(commandRegistry)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        GrizzlySmtpSession session = sessionProvider.getSmtpSession(ctx);
        SmtpInputBuffer smtpInput = getSmtpInputBuffer(ctx);
        smtpInput.appendMessage(ctx.getMessage());
        try {
            processingChain.process(smtpInput, session);
            if (smtpInput.isEmpty()) {
                session.flush();
            }
        } catch (SmtpException ex) {
            session.sendResponse(ex.getResponseCode(), ex.getResponseMessage());
            session.flush();
        }
        return ctx.getStopAction();
    }
}
