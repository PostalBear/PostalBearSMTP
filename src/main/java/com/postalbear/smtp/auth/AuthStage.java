/*
 */
package com.postalbear.smtp.auth;

import com.postalbear.smtp.exception.SmtpException;

/**
 * Represents authentication process stage.
 *
 * @author Grigory Fadeev
 */
public interface AuthStage<T extends AuthenticationHandler> {

    /**
     * Process given line in accordance with current stage of authentication process.
     *
     * @param line to process
     * @return true if more lines are required
     * @throws SmtpException indicates about error in authentication process
     */
    boolean handle(T handler, String line) throws SmtpException;
}
