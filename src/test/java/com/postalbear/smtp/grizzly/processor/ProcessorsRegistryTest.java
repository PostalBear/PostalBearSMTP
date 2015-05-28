package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.auth.AuthenticationHandler;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessorsRegistryTest {

    @Mock
    private GrizzlySmtpSession session;
    @Mock
    private AuthenticationHandler authHandler;

    private ProcessorsRegistry processorsRegistry = new ProcessorsRegistry();

    @Test
    public void testAuthInProgress() throws Exception {
        when(session.getAuthenticationHandler()).thenReturn(authHandler);
        assertTrue(processorsRegistry.getProcessor(session) instanceof AuthenticationProcessor);
    }

    @Test
    public void testWithDefaultProcessor() throws Exception {
        assertTrue(processorsRegistry.getProcessor(session) instanceof PipeliningProcessor);
    }
}