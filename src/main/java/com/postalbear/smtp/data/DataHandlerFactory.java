package com.postalbear.smtp.data;

import com.postalbear.smtp.SmtpSession;

/**
 * Implementation of this interface is intended to create fresh instances of DataHandler.
 *
 * @author Grigory Fadeev.
 */
public interface DataHandlerFactory {

    /**
     * Creates fresh instance of DataHandler.
     *
     * @param session to associate with
     * @return fresh instance of DataHandler
     */
    DataHandler create(SmtpSession session);
}
