/*
 */
package com.postalbear.smtp.auth;

import com.postalbear.smtp.exception.SmtpException;

/**
 * Represents authentication process stage.
 *
 * @author Grigory Fadeev
 */
public interface AuthState {

    /**
     * Implementations are intended to process given line.
     *
     * @param line to process
     * @return true if more lines are required
     * @throws SmtpException indicates about error in authentication process
     */
    boolean handle(String line) throws SmtpException;
}
