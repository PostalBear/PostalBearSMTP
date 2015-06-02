package com.postalbear.smtp.grizzly.data;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.data.DataHandlerFactory;

/**
 * @author Grigory Fadeev.
 */
public class GrizzlyDataHandlerFactory implements DataHandlerFactory {

    @Override
    public DataHandler create(SmtpSession session) {
        return new GrizzlyDataHandler(session);
    }
}
