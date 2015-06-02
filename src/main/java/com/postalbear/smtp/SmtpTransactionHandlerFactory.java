package com.postalbear.smtp;

/**
 * Implementations of this interface are intended to create SmtpTransactionHandler.
 *
 * @author Grigory Fadeev
 */
public interface SmtpTransactionHandlerFactory {

    /**
     * Creates new SmtpTransactionHandler instance
     *
     * @return SmtpTransactionHandler
     */
    SmtpTransactionHandler getHandler();
}
