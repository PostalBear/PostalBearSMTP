package com.postalbear.smtp;

import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.data.DataHandler;

/**
 * Interface of SMTP session.
 *
 * @author Grigory Fadeev
 */
public interface SmtpSession extends ConfigurationProvider, SmtpTransactionHandler {

    /**
     * @return true if client's helo received
     */
    boolean isClientHeloDone();

    /**
     * Associate given AuthenticationHandler with session.
     *
     * @param authenticationHandler handler or null
     */
    void setAuthenticationHandler(AuthenticationHandler authenticationHandler);

    /**
     * Marks session as successfully authenticated.
     */
    void setAuthenticated();

    /**
     * Associate given DataHandler with session.
     *
     * @param dataHandler handler or null
     */
    void setDataHandler(DataHandler dataHandler);

    /**
     * Check that current SMTP session is authenticated.
     *
     * @return true if authenticated
     */
    boolean isAuthenticated();

    /**
     * Start new mail transaction.
     */
    void startMailTransaction();

    /**
     * Reset state of current SMTP transaction.
     */
    void resetMailTransaction();

    /**
     * Check whether mail transaction is in progress.
     *
     * @return true if transaction is in progress
     */
    boolean isMailTransactionInProgress();

    /**
     * Return number of recipients.
     *
     * @return count of declared recipients.
     */
    int getRecipientsCount();

    /**
     * Send response to the client with default SMTP format (CODE MESSAGE).
     *
     * @param code    SMTP code
     * @param message to be sent to client
     */
    void sendResponse(int code, String message);

    /**
     * Send response to the client.
     * Useful if invoker need custom format of response message.
     *
     * @param message SMTP line to be set to client
     */
    void sendResponseAsString(String message);

    /**
     * Check whether connection is plain SMTP connection or security layer has been already installed.
     *
     * @return true if SSL/TLS is used
     */
    boolean isConnectionSecured();

    /**
     * Install security layer to the underlying connection
     */
    void startTls();

    /**
     * Force server to send content of response buffer back to client.
     */
    void flush();

    /**
     * Flush response buffer and close connection.
     */
    void closeSession();
}
