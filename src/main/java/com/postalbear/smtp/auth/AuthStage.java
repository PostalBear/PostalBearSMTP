/*
 */
package com.postalbear.smtp.auth;

import com.postalbear.smtp.exception.SmtpException;

/**
 * Represents authentication process stage.
 *
 * @author Grigory Fadeev
 */
public interface AuthStage<T extends AbstractAuthenticationHandler> {

    /**
     * Implementations are intended to process given line.
     *
     * @param line to process
     * @return true if more lines are required
     * @throws SmtpException indicates about error in authentication process
     */
    boolean handle(T handler, String line) throws SmtpException;
}
