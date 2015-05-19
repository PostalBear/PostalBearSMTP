package com.postalbear.smtp.auth;

import com.postalbear.smtp.SmtpProcessor;
import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * Base class for authentication handlers.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public abstract class AbstractAuthenticationHandler<T extends AuthStage> implements AuthenticationHandler, SmtpProcessor {

    // RFC 2554 explicitly states this:
    private static final String CANCEL_COMMAND = "*";
    //
    private final SmtpSession session;
    private T stage;

    /*
     * @param session      for which authentication process was started
     * @param reader       to read additional data from client
     * @param initialStage initial stage of authentication
     */
    public AbstractAuthenticationHandler(@NonNull SmtpSession session, @NonNull T initialStage) {
        this.session = session;
        this.stage = initialStage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(String line) throws SmtpException, IOException {
        if (stage.handle(this, line)) {
            session.setSmtpProcessor(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, SmtpSession session) throws IOException {
        try {
            String line = smtpInput.getSmtpLine();
            if (checkIsAuthCanceled(line) || !stage.handle(this, line)) {
                session.setSmtpProcessor(null);
            }
        } catch (SmtpException ex) {
            session.setSmtpProcessor(null);
            throw ex;
        }
    }

    private boolean checkIsAuthCanceled(String line) {
        if (CANCEL_COMMAND.equals(line)) {
            sendResponse(501, "Authentication canceled by client."); //see RFC4954
            return true;
        }
        return false;
    }

    /**
     * Send response back to the client.
     *
     * @param code    result code
     * @param message result message
     */
    public void sendResponse(int code, String message) {
        session.sendResponse(code, message);
    }

    /**
     * Mark authentication process as finished.
     */
    public void markAsSuccessful() {
        session.setAuthenticated();
        sendResponse(235, "2.7.0 Authentication successful.");
    }

    /**
     * Change stage of authentication process.
     *
     * @param stage new stage
     */
    public void setStage(@NonNull T stage) {
        this.stage = stage;
    }
}
