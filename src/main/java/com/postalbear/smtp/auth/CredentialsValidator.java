package com.postalbear.smtp.auth;

/**
 * Implementations are intended to check client credentials during handling of AUTH command.
 *
 * @author Grigory Fadeev
 */
public interface CredentialsValidator {

    /**
     * Checks if given credentials are valid.
     *
     * @param login    to check
     * @param password to check
     * @return true if valid
     */
    boolean validateCredentials(String login, String password);
}
