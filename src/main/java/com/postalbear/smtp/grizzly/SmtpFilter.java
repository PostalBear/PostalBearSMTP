package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpProcessor;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import java.io.IOException;

import static com.postalbear.smtp.grizzly.SmtpInputBuffer.getSmtpInputBuffer;

/**
 * Filter to process SMTP commands received from client.
 *
 * @author Grigory Fadeev
 */
public class SmtpFilter extends BaseFilter {

    private final SmtpSessionProvider sessionProvider;
    private final SmtpProcessor defaultProcessor;

    public SmtpFilter(@NonNull SmtpSessionProvider sessionProvider, SmtpProcessor defaultProcessor) {
        this.sessionProvider = sessionProvider;
        this.defaultProcessor = defaultProcessor;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        SmtpSession session = sessionProvider.getSmtpSession(ctx);
        SmtpInputBuffer smtpInput = getSmtpInputBuffer(ctx);
        smtpInput.appendMessage(ctx.getMessage());
        try {
            getProcessor(session).process(smtpInput, session);
            if (smtpInput.isEmpty()) {
                session.flush();
            }
        } catch (SmtpException ex) {
            session.sendResponse(ex.getResponseCode(), ex.getResponseMessage());
            session.flush();
        }
        return ctx.getStopAction();
    }

    private SmtpProcessor getProcessor(SmtpSession session) {
        if (session.getSmtpProcessor() != null) {
            return session.getSmtpProcessor();
        }
        return defaultProcessor;
    }
}
