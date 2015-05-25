package com.postalbear.smtp;

import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import com.postalbear.smtp.grizzly.auth.GrizzlyAuthenticationHandlerFactory;
import org.apache.commons.lang3.Validate;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

/**
 * Represents SMTP server configuration.
 * <p>
 * Once created configuration should not be changed.
 * Not marked as @Immutable since contains object references which could be changed from outside.
 *
 * @author Grigory Fadeev
 */
public class SmtpServerConfiguration {

    private String softwareName;
    private String hostName;
    //authentication related
    private AuthenticationHandlerFactory authenticationFactory;
    private boolean authenticationEnforced;
    //
    private SmtpTransactionHandlerFactory handlerFactory;
    private int maxMessageSize;
    private int maxRecipients;
    //security related
    private boolean smtpsEnabled;
    private boolean startTlsEnabled;
    private boolean startTlsEnforced;
    private SSLEngineConfigurator sslConfig;

    private SmtpServerConfiguration() {
    }

    /**
     * @return Software name, part of server's welcome banner
     */
    public String getSoftwareName() {
        return softwareName;
    }

    /**
     * @return Hostname, part of server's welcome banner
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @return instance of AuthenticationHandlerFactory, or null if not set
     */
    public AuthenticationHandlerFactory getAuthenticationFactory() {
        return authenticationFactory;
    }

    /**
     * @return true if authentication is mandatory
     */
    public boolean isAuthenticationEnforced() {
        return authenticationEnforced;
    }

    /**
     * @return instance of SmtpTransactionHandlerFactory to create handlers for processing client requests
     */
    public SmtpTransactionHandlerFactory getHandlerFactory() {
        return handlerFactory;
    }

    /**
     * @return max allowed message size, 0 means size is not restricted
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * @return max allowed recipients
     */
    public int getMaxRecipients() {
        return maxRecipients;
    }

    /**
     * @return true if server should run in secured (SMTPS) mode
     */
    public boolean isSmtpsEnabled() {
        return smtpsEnabled;
    }

    /**
     * @return true if server supports upgrade of plain to secured connection
     */
    public boolean isStartTlsEnabled() {
        return startTlsEnabled;
    }

    /**
     * @return true if STARTTLS extension is mandatory
     */
    public boolean isStartTlsEnforced() {
        return startTlsEnforced;
    }

    /**
     * @return instance of SSLEngineConfigurator to be used for SMTPS / STARTTLS, or null
     */
    public SSLEngineConfigurator getSslConfig() {
        return sslConfig;
    }

    /**
     * @return builder to create instance of SmtpServerConfiguration
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Builder for SmtpServerConfiguration.
     */
    public static final class Builder {

        private SmtpServerConfiguration configuration = new SmtpServerConfiguration();

        private Builder() {
        }

        /**
         * Configure software name for server's welcome banner.
         *
         * @param softwareName to be used
         * @return builder itself
         */
        public Builder setSoftwareName(String softwareName) {
            configuration.softwareName = softwareName;
            return this;
        }

        /**
         * Configure hostname for server's welcome banner.
         *
         * @param hostName to be used.
         * @return builder itself
         */
        public Builder setHostName(String hostName) {
            configuration.hostName = hostName;
            return this;
        }

        /**
         * Set AuthenticationHandlerFactory instance to enable optional SMTP authentication.
         * Optional - means that client should decide whether to proceed with or without authentication.
         *
         * @param authenticationFactory instance
         * @return builder itself
         */
        public Builder setAuthenticationFactory(AuthenticationHandlerFactory authenticationFactory) {
            if (authenticationFactory != null) {
                configuration.authenticationFactory = new GrizzlyAuthenticationHandlerFactory(authenticationFactory);
            } else {
                configuration.authenticationFactory = null;
            }
            return this;
        }

        /**
         * Enforce SMTP authentication.
         *
         * @param authenticationEnforced true to enforce
         * @return builder itself
         */
        public Builder setAuthenticationEnforced(boolean authenticationEnforced) {
            configuration.authenticationEnforced = authenticationEnforced;
            return this;
        }

        /**
         * Set instance of SmtpTransactionHandlerFactory, to process client requests.
         *
         * @param handlerFactory instance
         * @return builder itself
         */
        public Builder setHandlerFactory(SmtpTransactionHandlerFactory handlerFactory) {
            configuration.handlerFactory = handlerFactory;
            return this;
        }

        /**
         * Configures max allowed message size.
         * 0 (default state) - means no limit.
         *
         * @param maxMessageSize to use
         * @return builder itself
         */
        public Builder setMaxMessageSize(int maxMessageSize) {
            configuration.maxMessageSize = maxMessageSize;
            return this;
        }

        /**
         * Configures max allowed recipients count.
         * 0 (default state) - means no limit.
         *
         * @param maxRecipients to use
         * @return builder itself
         */
        public Builder setMaxRecipients(int maxRecipients) {
            configuration.maxRecipients = maxRecipients;
            return this;
        }

        /**
         * If enabled, Server will support only secured SMTP communication (SMTPS).
         *
         * @param smtpsEnabled true to enable
         * @return builder itself
         */
        public Builder setSmtpsEnabled(boolean smtpsEnabled) {
            configuration.smtpsEnabled = smtpsEnabled;
            return this;
        }

        /**
         * If enabled, Server will support STARTTLS extension, to allow upgrade plain SMTP connection to secured one.
         *
         * @param startTlsEnabled true to enable
         * @return builder itself
         */
        public Builder setStartTlsEnabled(boolean startTlsEnabled) {
            configuration.startTlsEnabled = startTlsEnabled;
            return this;
        }

        /**
         * If enabled, Server will enforce usage of STARTTLS extension.
         *
         * @param startTlsEnforced true to enforce
         * @return builder itself
         */
        public Builder setStartTlsEnforced(boolean startTlsEnforced) {
            configuration.startTlsEnforced = startTlsEnforced;
            return this;
        }

        /**
         * Set SSLEngineConfigurator which is mandatory for SMTPS and STARTTLS.
         *
         * @param sslConf to be used with SMTPS or STARTTLS
         * @return builder itself
         */
        public Builder setSslConfiguration(SSLEngineConfigurator sslConf) {
            configuration.sslConfig = sslConf;
            return this;
        }

        /**
         * Create instance of SmtpServerConfiguration.
         *
         * @return resulting configuration
         * @throws NullPointerException     if mandatory option is null and therefore configuration is invalid
         * @throws IllegalArgumentException if configuration contains contradictions or invalid
         */
        public SmtpServerConfiguration buildConfiguration() {
            SmtpServerConfiguration result = configuration;
            Validate.notEmpty(result.softwareName, "SoftwareName must be set");
            Validate.notEmpty(result.hostName, "Hostname must be set");
            Validate.notNull(result.handlerFactory, "HandlerFactory can't be null");
            //
            Validate.isTrue(result.maxMessageSize >= 0, "MaxMessageSize should be >= 0");
            //
            Validate.isTrue(result.maxRecipients >= 0, "MaxRecipients should be >= 0");
            if (result.authenticationEnforced) {
                Validate.notNull(result.authenticationFactory,
                                 "Authentication can't be enforced when AuthenticationFactory is not set");
            }
            if (result.smtpsEnabled) {
                Validate.notNull(result.sslConfig, "SSL configuration must be set when SMTPS enabled");
                Validate.isTrue(!result.startTlsEnabled, "StartTLS can't be enabled for SMTPS server");
                Validate.isTrue(!result.startTlsEnforced, "StartTLS can't be enforced for SMTPS server");
            }
            if (result.startTlsEnabled) {
                Validate.notNull(result.sslConfig, "SSL configuration must be set when STARTTLS enabled");
            }
            if (result.startTlsEnforced) {
                Validate.isTrue(result.startTlsEnabled, "StartTLS can't be enforced when not enabled");
            }
            configuration = new SmtpServerConfiguration();
            return result;
        }
    }
}
