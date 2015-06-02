package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.MessageDecoder;

import java.io.IOException;

/**
 * Implements receiving of mail message in NIO way.
 * <p>
 * <i>Implementation Note</i>: Current state of implementation has serious drawback:
 * It does not support streaming, but accumulates message content in memory.
 *
 * @author Grigory Fadeev.
 */
public class DataProcessor implements SmtpProcessor {

    private final MessageDecoder messageDecoder = MessageDecoder.getInstance();

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, GrizzlySmtpSession session) throws IOException {
        DataHandler handler = session.getDataHandler();
        if (smtpInput.hasEnoughData(messageDecoder)) {
            handler.processData(smtpInput.getData(messageDecoder));
        }
    }
}

