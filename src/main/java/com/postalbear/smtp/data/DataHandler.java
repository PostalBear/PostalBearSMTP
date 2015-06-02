package com.postalbear.smtp.data;

import java.io.IOException;

/**
 * Implementation of this interface is intended to process mail message submitted by client.
 *
 * @author Grigory Fadeev.
 */
public interface DataHandler {

    /**
     * Kickstarts receiving of mail message.
     */
    void kickstartData();

    /**
     * Process received portion of message / whole message, depends on implementation.
     *
     * @param chunk part of or complete message in bytes
     * @throws IOException if something went wrong
     */
    void processData(byte[] chunk) throws IOException;
}
