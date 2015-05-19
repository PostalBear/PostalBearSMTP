package com.postalbear.smtp;

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
     * Implementation note: method should not be invoked when no SMTP line is available at input !
     *
     * @param smtpInput
     * @param session
     * @throws IOException
     */
    void process(SmtpInput smtpInput, SmtpSession session) throws IOException;
}
