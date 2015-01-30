/*
 */
package com.postalbear.smtp;

import com.postalbear.smtp.io.SmtpLineReader;

import java.io.InputStream;

/**
 * Implementations provide access to SMTP input buffer via @see SmtpLineReader or @see InputStream.
 *
 * @author Grigory Fadeev
 */
public interface SmtpInput {

    /**
     * To get raw data from buffer via InputStream.
     * Quite stupid since NIO framework is used under the hood, but for now it's enough.
     *
     * @return stream to access data
     */
    InputStream getInputStream();

    /**
     * To get SMTP lines from income buffer.
     *
     * @return reader to read SMTP lines.
     */
    SmtpLineReader getSmtpLineReader();
}
