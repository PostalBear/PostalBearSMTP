package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.MessageDecoder;

import java.io.IOException;

/**
 * If client issued Data command earlier, this processor is intended to receive mail message, in NIO way.
 *
 * @author Grigory Fadeev.
 */
public class MessageReceivingProcessor implements SmtpProcessor {

    private final MessageDecoder messageDecoder = MessageDecoder.getInstance();
    private final SmtpProcessor next;

    /**
     * Constructs instance of MessageReceivingProcessor class.
     *
     * @param next processor in chain
     */
    public MessageReceivingProcessor(SmtpProcessor next) {
        this.next = next;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, GrizzlySmtpSession session) throws IOException {
        if (isMessageReceivingInProgress(session)) {
            DataHandler handler = session.getDataHandler();
            if (smtpInput.hasEnoughData(messageDecoder)) {
                handler.processData(smtpInput.getData(messageDecoder));
            }
        }
        next.process(smtpInput, session);
    }

    private boolean isMessageReceivingInProgress(GrizzlySmtpSession session) {
        return session.getDataHandler() != null;
    }
}

