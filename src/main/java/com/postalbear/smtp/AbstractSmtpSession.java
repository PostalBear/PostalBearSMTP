package com.postalbear.smtp;

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
    private String clientHelo;
    private int recipientsCount = 0;
    private boolean authenticated;
    private SmtpTransactionHandler handler;

    public AbstractSmtpSession(final ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
        this.configuration = configurationProvider.getConfiguration();
    }

    @Override
    public void setAuthenticated() {
        authenticated = true;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void startMailTransaction() {
        recipientsCount = 0;
        configuration = configurationProvider.getConfiguration();
        handler = configuration.getHandlerFactory().getHandler();
        handler.helo(clientHelo);
    }

    @Override
    public boolean isMailTransactionInProgress() {
        return handler != null;
    }

    @Override
    public void resetMailTransaction() {
        handler = null;
        recipientsCount = 0;
    }

    @Override
    public SmtpServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public int getRecipientsCount() {
        return recipientsCount;
    }

    @Override
    public void helo(String clientHelo) {
        this.clientHelo = clientHelo;
    }

    @Override
    public void from(InternetAddress from) {
        handler.from(from);
    }

    @Override
    public void messageSize(int size) {
        handler.messageSize(size);
    }

    @Override
    public void recipient(InternetAddress recipient) {
        handler.recipient(recipient);
        recipientsCount++;
    }

    @Override
    public void data(InputStream data) throws IOException {
        handler.data(data);
    }
}
