package com.postalbear.smtp.auth;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Base class for authentication handlers.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public abstract class AbstractAuthenticationHandler<T extends AuthStage> implements AuthenticationHandler {

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
    public void kickstartAuth(String smtpLine) throws SmtpException {
        stage.handle(this, smtpLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processAuth(String smtpLine) throws SmtpException {
        if (checkIsCanceled(smtpLine)) {
            session.sendResponse(501, "Authentication canceled by client."); //see RFC4954
            session.setAuthenticationHandler(null);
            return false;
        }
        if (!stage.handle(this, smtpLine)) {
            session.setAuthenticationHandler(null);
            return false;
        }
        return true;
    }

    private boolean checkIsCanceled(String smtpLine) {
        return CANCEL_COMMAND.equals(smtpLine);
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
     * Change stage of authentication process.
     *
     * @param stage new stage
     */
    public void setStage(@NonNull T stage) {
        this.stage = stage;
    }

    /**
     * Mark authentication process as finished.
     */
    protected void markAsSuccessful() {
        session.setAuthenticated();
        session.sendResponse(235, "2.7.0 Authentication successful.");
    }
}
