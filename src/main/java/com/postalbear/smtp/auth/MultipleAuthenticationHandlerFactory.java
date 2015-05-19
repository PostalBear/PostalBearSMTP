/*
 */
package com.postalbear.smtp.auth;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.apache.commons.lang3.Validate;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Facade class for several authentication factories.
 * Simply delegates authentication process to underlying factory depending on mechanism.
 *
 * @author Grigory Fadeev
 */
public class MultipleAuthenticationHandlerFactory implements AuthenticationHandlerFactory {

    private final Map<String, AuthenticationHandlerFactory> authMechanisms = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    /**
     * Construct instance of MultipleAuthenticationHandlerFactory class.
     *
     * @param factories delegates
     */
    public MultipleAuthenticationHandlerFactory(AuthenticationHandlerFactory... factories) {
        Validate.notEmpty(factories);
        Validate.noNullElements(factories);
        for (AuthenticationHandlerFactory factory : factories) {
            for (String mechanism : factory.getAuthenticationMechanisms()) {
                authMechanisms.putIfAbsent(mechanism.toUpperCase(Locale.ENGLISH), factory);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getAuthenticationMechanisms() {
        return authMechanisms.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AuthenticationHandler create(String mechanism, SmtpSession session) {
        if (!getAuthenticationMechanisms().contains(mechanism)) {
            throw new SmtpException(504, "5.5.4 The requested authentication mechanism is not supported");
        }
        return authMechanisms.get(mechanism).create(mechanism, session);
    }
}
