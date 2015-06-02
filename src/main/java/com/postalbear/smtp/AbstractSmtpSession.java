package com.postalbear.smtp;

import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.data.DataHandler;

import javax.annotation.concurrent.NotThreadSafe;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;

/**
 * SMTP session, independent from Transport layer.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public abstract class AbstractSmtpSession implements SmtpSession {

    private final ConfigurationProvider configurationProvider;
    private SmtpServerConfiguration configuration;
    //session state
    private String clientHelo;
    private int recipientsCount;
    private AuthenticationHandler authenticationHandler;
    private boolean authenticated;
    private DataHandler dataHandler;
    private SmtpTransactionHandler handler;

    public AbstractSmtpSession(final ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
        this.configuration = configurationProvider.getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startMailTransaction() {
        recipientsCount = 0;
        configuration = configurationProvider.getConfiguration();
        handler = configuration.getHandlerFactory().getHandler();
        handler.helo(clientHelo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMailTransactionInProgress() {
        return handler != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetMailTransaction() {
        recipientsCount = 0;
        handler = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmtpServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRecipientsCount() {
        return recipientsCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void helo(String clientHelo) {
        this.clientHelo = clientHelo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClientHeloDone() {
        return clientHelo != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void from(InternetAddress from) {
        handler.from(from);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageSize(int size) {
        handler.messageSize(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recipient(InternetAddress recipient) {
        handler.recipient(recipient);
        recipientsCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    /**
     * @return associated instance of DataHandler
     */
    public DataHandler getDataHandler() {
        return dataHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void data(InputStream data) throws IOException {
        handler.data(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * @return associated instance of AuthenticationHandler
     */
    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAuthenticated() {
        authenticated = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
}
