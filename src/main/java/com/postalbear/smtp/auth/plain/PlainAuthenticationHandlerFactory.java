package com.postalbear.smtp.auth.plain;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

/**
 * AuthenticationHandler factory for PLAIN SASL mechanism.
 *
 * @author Grigory Fadeev
 */
public class PlainAuthenticationHandlerFactory implements AuthenticationHandlerFactory {

    private static final Set<String> MECHANISM = Collections.singleton("PLAIN");

    private CredentialsValidator credentialsValidator;

    /**
     * Constructs instance of PlainAuthenticationHandlerFactory.
     *
     * @param credentialsValidator to check credentials with
     */
    public PlainAuthenticationHandlerFactory(@NonNull CredentialsValidator credentialsValidator) {
        this.credentialsValidator = credentialsValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAuthenticationMechanisms() {
        return MECHANISM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationHandler create(String mechanism, SmtpSession session) {
        if (!getAuthenticationMechanisms().contains(mechanism)) {
            throw new SmtpException(504, "5.5.4 The requested authentication mechanism is not supported");
        }
        return new PlainAuthenticationHandler(session, credentialsValidator);
    }
}
