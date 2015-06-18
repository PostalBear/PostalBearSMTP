package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.SmtpLineDecoder;

import java.io.IOException;

/**
 * Performs authentication procedure, if one is in progress, in NIO way.
 *
 * @author Grigory Fadeev.
 */
public class AuthenticationProcessor implements SmtpProcessor {

    private final SmtpLineDecoder decoder = SmtpLineDecoder.getInstance();
    private final SmtpProcessor next;

    /**
     * Constructs instance of AuthenticationProcessor class.
     *
     * @param next processor in chain
     */
    public AuthenticationProcessor(SmtpProcessor next) {
        this.next = next;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(SmtpInput smtpInput, GrizzlySmtpSession session) throws IOException {
        if (isAuthenticationInProgress(session)) {
            AuthenticationHandler handler = session.getAuthenticationHandler();
            try {
                while (smtpInput.hasEnoughData(decoder) && handler.processAuth(smtpInput.getData(decoder))) ;
            } catch (SmtpException ex) {
                session.setAuthenticationHandler(null);
                throw ex;
            }
        }
        next.process(smtpInput, session);
    }

    private boolean isAuthenticationInProgress(GrizzlySmtpSession session) {
        return session.getAuthenticationHandler() != null;
    }
}
