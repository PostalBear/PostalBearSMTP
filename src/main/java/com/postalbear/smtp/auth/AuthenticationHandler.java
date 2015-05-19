package com.postalbear.smtp.auth;

import com.postalbear.smtp.exception.SmtpException;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * The interface that enables challenge-response communication necessary for SMTP AUTH.<p>
 * Since the authentication process is stateful, implementations of this interface are stateful too.<br>
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
@NotThreadSafe
public interface AuthenticationHandler {

    /**
     * Start SMTP authentication procedure.
     *
     * @param smtpLine which triggers authentication
     * @throws SmtpException
     */
    void start(String smtpLine) throws SmtpException;
}
