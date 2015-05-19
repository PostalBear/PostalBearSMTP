/*
 */
package com.postalbear.smtp;

import java.io.InputStream;

/**
 * Implementations provide access to SMTP input buffer via @see SmtpLineReader or @see InputStream.
 *
 * @author Grigory Fadeev
 */
public interface SmtpInput {

    /**
     * Check whether next SMTP line is available for reading.
     *
     * @return true if complete SMTP line is available
     */
    boolean hasNextSmtpLine();

    /**
     * Reads next SMTP line from input.
     *
     * @return SMTP line
     */
    String getSmtpLine();

    /**
     * Check whether input contains any data.
     *
     * @return true if input does not contain any data
     */
    boolean isEmpty();

    /**
     * To get raw data from buffer via InputStream.
     * Quite stupid since NIO framework is used under the hood, but for now it's enough.
     *
     * @return stream to access data
     */
    InputStream getInputStream();
}
