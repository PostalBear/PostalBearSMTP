package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;

import java.io.IOException;

/**
 * Callback which is invoked on new chunk of data received from client.
 *
 * @author Grigory Fadeev
 */
public interface SmtpProcessor {

    /**
     * Concrete implementations are intended to process available data from SmtpInput.
     *
     * @param smtpInput to read data from
     * @param session   to process
     * @throws IOException if some portion of data can't be read
     */
    void process(SmtpInput smtpInput, GrizzlySmtpSession session) throws IOException;
}
