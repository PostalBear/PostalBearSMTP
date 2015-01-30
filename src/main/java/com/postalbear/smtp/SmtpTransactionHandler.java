package com.postalbear.smtp;

import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;

/**
 * Defines set of callback methods invoked by SMTP command handlers.
 *
 * @author Grigory Fadeev
 */
public interface SmtpTransactionHandler {

    /**
     * Callback for HELO|EHLO commands.
     *
     * @param clientHelo
     */
    void helo(String clientHelo);

    /**
     * Callback for SIZE parameter of MAIL command.
     *
     * @param size
     */
    void messageSize(int size);

    /**
     * Callback method for MAIL FROM command.
     *
     * @param from SMTP envelope from
     */
    void from(InternetAddress from);

    /**
     * Callback method for MAIL FROM command.
     *
     * @param recipient
     */
    void recipient(InternetAddress recipient);

    /**
     * Callback for DATA command.
     *
     * @param data
     * @throws java.io.IOException
     */
    void data(InputStream data) throws IOException;
}
