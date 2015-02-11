package com.postalbear.smtp.auth;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.io.SmtpLineReader;
import lombok.NonNull;

import java.io.IOException;

/**
 * Base for authentication handlers.
 *
 * @author Grigory Fadeev
 */
public abstract class AbstractAuthenticationHandler<T extends AuthStage> implements AuthenticationHandler {

    // RFC 2554 explicitly states this:
    private static final String CANCEL_COMMAND = "*";
    //
    private final SmtpSession session;
    private final SmtpLineReader reader;
    private T stage;

    /*
     * @param session      for which authentication process was started
     * @param reader       to read additional data from client
     * @param initialStage initial stage of authentication
     */
    public AbstractAuthenticationHandler(@NonNull SmtpSession session, @NonNull SmtpLineReader reader, @NonNull T initialStage) {
        this.session = session;
        this.reader = reader;
        this.stage = initialStage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(String line) throws SmtpException, IOException {
        String currentLine = line;
        while (!checkCancelCommand(currentLine) && stage.handle(this, currentLine)) {
            currentLine = reader.readLine();
        }
    }

    /**
     * @param line
     * @return only to make javac happy.
     */
    private boolean checkCancelCommand(String line) {
        if (CANCEL_COMMAND.equals(line)) {
            throw new SmtpException(501, "Authentication canceled by client."); //see RFC4954
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
        session.flush();
    }

    /**
     * Mark authentication process as finished.
     */
    public void completeAuthentication() {
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
