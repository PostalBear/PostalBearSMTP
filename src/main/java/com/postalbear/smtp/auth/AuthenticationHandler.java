package com.postalbear.smtp.auth;

import com.postalbear.smtp.exception.SmtpException;

import java.io.IOException;

/**
 * The interface that enables challenge-response communication necessary for SMTP AUTH.<p>
 * Since the authentication process is stateful, implementations of this interface are stateful too.<br>
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public interface AuthenticationHandler {

    /**
     * Initially called using an input string in the RFC4954 form: "AUTH \<mechanism\> [initial-response]".
     *
     * @param smtpLine line to process
     * @throws SmtpException       if authentication attempt failed
     * @throws java.io.IOException in case if can't read/write data
     */
    void auth(String smtpLine) throws SmtpException, IOException;
}
