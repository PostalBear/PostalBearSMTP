package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.processor.ProcessorsRegistry;
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
    private final ProcessorsRegistry processors;

    public SmtpFilter(@NonNull SmtpSessionProvider sessionProvider, ProcessorsRegistry processors) {
        this.sessionProvider = sessionProvider;
        this.processors = processors;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        GrizzlySmtpSession session = sessionProvider.getSmtpSession(ctx);
        SmtpInputBuffer smtpInput = getSmtpInputBuffer(ctx);
        smtpInput.appendMessage(ctx.getMessage());
        try {
            processors.getProcessor(session).process(smtpInput, session);
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
