package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.Decoder;
import com.postalbear.smtp.grizzly.codec.SmtpLineDecoder;

import java.io.IOException;

/**
 * Redirects available SMTP lines to authentication handler linked with session.
 *
 * @author Grigory Fadeev.
 */
public class AuthenticationProcessor implements SmtpProcessor {

    private final Decoder<String> decoder = SmtpLineDecoder.getInstance();

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, SmtpSession session) throws IOException {
        AuthenticationHandler handler = session.getAuthenticationHandler();
        try {
            while (smtpInput.hasEnoughData(decoder)) {
                if (!handler.processAuth(smtpInput.getData(decoder))) {
                    return;
                }
            }
        } catch (SmtpException ex) {
            session.setAuthenticationHandler(null);
            throw ex;
        }
    }
}
