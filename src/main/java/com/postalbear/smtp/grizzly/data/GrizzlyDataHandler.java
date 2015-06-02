package com.postalbear.smtp.grizzly.data;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.io.DotUnstuffingInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Grigory Fadeev.
 */
public class GrizzlyDataHandler implements DataHandler {

    private final SmtpSession session;

    /**
     * Constructs GrizzlyDataHandler instance.
     *
     * @param session to associate with
     */
    public GrizzlyDataHandler(SmtpSession session) {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kickstartData() {
        session.setDataHandler(this);
        session.sendResponse(354, "End data with <CR><LF>.<CR><LF>");
        session.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processData(byte[] chunk) throws IOException {
        session.setDataHandler(null);
        session.data(new DotUnstuffingInputStream(new ByteArrayInputStream(chunk)));
        session.sendResponse(250, "OK");
        session.resetMailTransaction();
    }
}
