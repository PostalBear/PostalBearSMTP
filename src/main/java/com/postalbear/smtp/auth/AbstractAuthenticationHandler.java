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
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    // RFC 2554 explicitly states this:
    private static final String CANCEL_COMMAND = "*";
    //
    private final SmtpSession session;
    private final SmtpLineReader inputLineReader;
    private final CredentialsValidator validator;

    private AuthState state;

    /**
     * @param session
     * @param inputLineReader
     * @param validator
     */
    public AbstractAuthenticationHandler(@NonNull SmtpSession session,
                                         @NonNull SmtpLineReader inputLineReader,
                                         @NonNull CredentialsValidator validator) {
        this.session = session;
        this.inputLineReader = inputLineReader;
        this.validator = validator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void auth(String smtpLine) throws SmtpException, IOException {
        String line = smtpLine;
        while (true) {
            if (CANCEL_COMMAND.equals(line)) {
                throw new SmtpException(501, "Authentication canceled by client."); //see RFC4954
            }
            if (!getState().handle(line)) {
                return;
            }
            line = inputLineReader.readLine();
        }
    }

    protected void validateCredentials(String login, String password) throws SmtpException {
        if (!validator.validateCredentials(login, password)) {
            throw new SmtpException(535, "5.7.8 Authentication failure, invalid credentials");
        }
    }

    protected void sendResponse(int code, String message) {
        session.sendResponse(code, message);
        session.flush();
    }

    protected void setState(@NonNull AuthState state) {
        this.state = state;
    }

    private AuthState getState() {
        if (state == null) {
            throw new IllegalStateException("Authentication state not set");
        }
        return state;
    }
}
