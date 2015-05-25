package com.postalbear.smtp.grizzly.auth;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import lombok.NonNull;

import java.util.Set;

/**
 * Produces instances of AuthenticationProcessor class.
 *
 * @author Grigory Fadeev.
 */
public class GrizzlyAuthenticationHandlerFactory implements AuthenticationHandlerFactory {

    private final AuthenticationHandlerFactory delegatingFactory;

    /**
     * Constructs instance of GrizzlyAuthenticationHandlerFactory class.
     *
     * @param delegatingFactory reference
     */
    public GrizzlyAuthenticationHandlerFactory(@NonNull AuthenticationHandlerFactory delegatingFactory) {
        this.delegatingFactory = delegatingFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAuthenticationMechanisms() {
        return delegatingFactory.getAuthenticationMechanisms();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationHandler create(String mechanism, SmtpSession session) {
        return new AuthenticationProcessor(delegatingFactory.create(mechanism, session), session);
    }
}
