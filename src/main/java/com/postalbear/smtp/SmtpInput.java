/*
 */
package com.postalbear.smtp;

import com.postalbear.smtp.grizzly.codec.Decoder;

import java.io.InputStream;

/**
 * Implementations provide access to SMTP input buffer.
 *
 * @author Grigory Fadeev
 */
public interface SmtpInput {

    /**
     * Check whether input is empty.
     *
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * Check whether input has enough available data for reading.
     *
     * @return true if enough
     */
    boolean hasEnoughData(Decoder<?> decoder);

    /**
     * Reads next data chunk from input.
     *
     * @return data chunk
     */
    <T> T getData(Decoder<T> decoder);

    /**
     * To get raw data from buffer via InputStream.
     * Quite stupid since NIO framework is used under the hood, but for now it's enough.
     *
     * @return stream to access data
     */
    InputStream getInputStream();
}
