package com.postalbear.smtp;

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
     * Check that current SMTP session is authenticated.
     *
     * @return true if authenticated
     */
    boolean isAuthenticated();

    /**
     * Marks session as successfully authenticated.
     */
    void setAuthenticated();

    /**
     * Associate custom SmtpProcessor with current session which should be used for processing of incoming data.
     *
     * @param smtpProcessor
     */
    void setSmtpProcessor(SmtpProcessor smtpProcessor);

    /**
     * @return associated SmtpProcessor instance or null
     */
    SmtpProcessor getSmtpProcessor();

    /**
     * Check whether mail transaction is in progress.
     *
     * @return true if transaction is in progress
     */
    boolean isMailTransactionInProgress();

    /**
     * Start new mail transaction.
     */
    void startMailTransaction();

    /**
     * Reset state of current SMTP transaction.
     */
    void resetMailTransaction();

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
