package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;

import java.io.IOException;

/**
 * Intention of this interface is to introduce real NIO processing.
 * As a good example I could mention AUTH command:
 * Usually it requires several request/response round trips to authenticate client.
 * SubethaSMTP code is implemented in a way to use blocking read for reading multiple SMTP lines.
 * It's not very efficient, since processing thread is blocked.
 * So let's make it NIO way and introduce callback to process available data.
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
