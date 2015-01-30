package com.postalbear.smtp;

import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import javax.annotation.concurrent.Immutable;

/**
 * Represents SMTP server configuration.
 *
 * @author Grigory Fadeev
 */
@Immutable
public class SmtpServerConfiguration {

    private String softwareName;
    private String hostName;
    private AuthenticationHandlerFactory authenticationFactory;
    /* force client to authenticate first */
    private boolean authenticationEnforced;
    private SmtpTransactionHandlerFactory handlerFactory;
    /* max message size allowed for this server in bytes */
    private int maxMessageSize;
    /* maximal allowed recipients for single mail transaction */
    private int maxRecipients;
    private boolean smtpsEnabled;
    /* allow StartTLS command */
    private boolean startTlsEnabled;
    /* if true, server forces client to use STARTTLS*/
    private boolean startTlsEnforced;
    /*SSL configuration to be used with server*/
    private SSLEngineConfigurator sslConfig;

    private SmtpServerConfiguration() {
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public String getHostName() {
        return hostName;
    }

    public AuthenticationHandlerFactory getAuthenticationFactory() {
        return authenticationFactory;
    }

    public boolean isAuthenticationEnforced() {
        return authenticationEnforced;
    }

    public SmtpTransactionHandlerFactory getHandlerFactory() {
        return handlerFactory;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public int getMaxRecipients() {
        return maxRecipients;
    }

    public boolean isSmtpsEnabled() {
        return smtpsEnabled;
    }

    public boolean isStartTlsEnabled() {
        return startTlsEnabled;
    }

    public boolean isStartTlsEnforced() {
        return startTlsEnforced;
    }

    public SSLEngineConfigurator getSslConfig() {
        return sslConfig;
    }

    /**
     * @return
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     *
     */
    public static class Builder {

        private SmtpServerConfiguration configuration = new SmtpServerConfiguration();

        private Builder() {
        }

        public Builder setSoftwareName(@NonNull String softwareName) {
            configuration.softwareName = softwareName;
            return this;
        }

        public Builder setHostName(@NonNull String hostName) {
            configuration.hostName = hostName;
            return this;
        }

        public Builder setAuthenticationFactory(@NonNull AuthenticationHandlerFactory authenticationFactory) {
            configuration.authenticationFactory = authenticationFactory;
            return this;
        }

        public Builder setAuthenticationEnforced(boolean authenticationEnforced) {
            configuration.authenticationEnforced = authenticationEnforced;
            return this;
        }

        public Builder setHandlerFactory(@NonNull SmtpTransactionHandlerFactory handlerFactory) {
            configuration.handlerFactory = handlerFactory;
            return this;
        }

        public Builder setMaxMessageSize(int maxMessageSize) {
            configuration.maxMessageSize = maxMessageSize;
            return this;
        }

        public Builder setMaxRecipients(int maxRecipients) {
            configuration.maxRecipients = maxRecipients;
            return this;
        }

        public Builder setSmtpsEnabled(boolean smtpsEnabled) {
            configuration.smtpsEnabled = smtpsEnabled;
            return this;
        }

        public Builder setStartTlsEnabled(boolean startTlsEnabled) {
            configuration.startTlsEnabled = startTlsEnabled;
            return this;
        }

        public Builder setStartTlsEnforced(boolean startTlsEnforced) {
            configuration.startTlsEnforced = startTlsEnforced;
            return this;
        }

        public Builder setSslConfiguration(SSLEngineConfigurator sslConf) {
            configuration.sslConfig = sslConf;
            return this;
        }

        public SmtpServerConfiguration buildConfiguration() {
            SmtpServerConfiguration instance = configuration;
            Validate.notEmpty(instance.softwareName, "softwareName is blank");
            Validate.notEmpty(instance.hostName, "serverName is blank");
            Validate.notNull(instance.handlerFactory, "handlerFactory is null");
            //
            Validate.isTrue(Integer.MAX_VALUE >= instance.maxMessageSize && instance.maxMessageSize >= 0,
                    "maxMessageSize should >=0");
            //
            Validate.isTrue(Integer.MAX_VALUE >= instance.maxRecipients && instance.maxRecipients >= 0,
                    "maxRecipients should >=0");
            if (instance.authenticationEnforced) {
                Validate.notNull(instance.authenticationFactory,
                        "Authentication can't be enabled when AuthenticationFactory is not configured");
            }
            if (instance.smtpsEnabled) {
                Validate.notNull(instance.sslConfig, "SSL configuration should be set if SMTPS enabled");
                Validate.isTrue(!instance.startTlsEnabled, "StartTLS can't be enabled for SMTPS server");
                Validate.isTrue(!instance.startTlsEnforced, "StartTLS can't be enforced for SMTPS server");
            }
            if (instance.startTlsEnforced) {
                Validate.notNull(instance.sslConfig, "SSL configuration should be set if STARTTLS enabled");
                Validate.isTrue(instance.startTlsEnabled, "StartTLS can't be enforced if not enabled");
            }
            configuration = new SmtpServerConfiguration();
            return instance;
        }
    }
}
