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
     * Starts SMTP authentication procedure.
     *
     * @param smtpLine to process
     * @throws SmtpException if authentication failed due to some reasons
     */
    void kickstartAuth(String smtpLine) throws SmtpException;

    /**
     * Continue SMTP authentication procedure.
     *
     * @param smtpLine to process
     * @return true if more lines needed
     * @throws SmtpException if authentication failed due to some reasons
     */
    boolean processAuth(String smtpLine) throws SmtpException;
}
