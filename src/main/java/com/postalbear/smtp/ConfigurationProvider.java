package com.postalbear.smtp;

/**
 * Interface to access current SMTP server configuration.
 *
 * @author Grigory Fadeev
 */
public interface ConfigurationProvider {

    /**
     * Return current SMTP server configuration.
     *
     * @return configuration
     */
    SmtpServerConfiguration getConfiguration();
}
