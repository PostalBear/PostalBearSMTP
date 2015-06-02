package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.command.CommandRegistryFactory;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Contains supported SmtpProcessors.
 *
 * @author Grigory Fadeev.
 */
@ThreadSafe
public class ProcessorsRegistry {

    private final AuthenticationProcessor authProcessor = new AuthenticationProcessor();
    private final DataProcessor dataProcessor = new DataProcessor();
    private final PipeliningProcessor defaultProcessor = new PipeliningProcessor(CommandRegistryFactory.create());

    /**
     * Based on SMTP state returns SmtpProcessor responsible for further processing.
     *
     * @param session to check
     * @return processor
     */
    public SmtpProcessor getProcessor(GrizzlySmtpSession session) {
        if (session.getAuthenticationHandler() != null) {
            return authProcessor;
        } else if (session.getDataHandler() != null) {
            return dataProcessor;
        }
        return defaultProcessor;
    }
}
