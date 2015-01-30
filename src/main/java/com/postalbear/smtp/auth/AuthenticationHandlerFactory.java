package com.postalbear.smtp.auth;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;

import java.util.Set;

/**
 * The factory interface for creating authentication handlers.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 * @author Grigory Fadeev
 */
public interface AuthenticationHandlerFactory {

    /**
     * Implementations should return the supported mechanisms.
     *
     * @return the supported authentication mechanisms, names <b>must</b> be in upper case.
     */
    Set<String> getAuthenticationMechanisms();

    /**
     * Create a fresh instance of authentication handler.
     *
     * @param session   for which authentication process is started
     * @param mechanism to use
     * @param input     line which triggered AUTH command
     * @return authentication handler
     */
    AuthenticationHandler create(String mechanism, SmtpSession session, SmtpInput input);
}
