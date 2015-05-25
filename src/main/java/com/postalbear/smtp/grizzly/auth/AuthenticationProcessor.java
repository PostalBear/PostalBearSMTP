package com.postalbear.smtp.grizzly.auth;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpProcessor;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.codec.Decoder;
import com.postalbear.smtp.grizzly.codec.SmtpLineDecoder;

import java.io.IOException;

/**
 * Intended to decouple Grizzly related code from SMTP authentication.
 *
 * @author Grigory Fadeev.
 */
public class AuthenticationProcessor implements AuthenticationHandler, SmtpProcessor {

    private final Decoder<String> decoder = SmtpLineDecoder.getInstance();
    private final AuthenticationHandler delegate;
    private final SmtpSession session;

    /**
     * Constructs instance of AuthenticationProcessor class.
     *
     * @param delegate which performs SMTP authentication
     * @param session  associated session
     */
    public AuthenticationProcessor(AuthenticationHandler delegate, SmtpSession session) {
        this.delegate = delegate;
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean processAuthentication(String smtpLine) throws SmtpException {
        boolean needMoreLines = delegate.processAuthentication(smtpLine);
        if (needMoreLines) {
            session.setSmtpProcessor(this);
        }
        return needMoreLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, SmtpSession session) throws IOException {
        try {
            while (smtpInput.hasEnoughData(decoder)) {
                if (!processAuthentication(smtpInput.getData(decoder))) {
                    session.setSmtpProcessor(null);
                    return;
                }
            }
        } catch (SmtpException ex) {
            session.setSmtpProcessor(null);
            throw ex;
        }
    }
}
