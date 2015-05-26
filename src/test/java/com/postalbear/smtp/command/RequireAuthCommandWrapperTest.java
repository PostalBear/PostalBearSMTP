/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class RequireAuthCommandWrapperTest {

    @Mock
    private Command command;
    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @InjectMocks
    private RequireAuthCommandWrapper wrapper;

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
    }

    @Test(expected = SmtpException.class)
    public void testPlainConnectionAndTLSEnforced() throws Exception {
        when(configuration.isAuthenticationEnforced()).thenReturn(true);
        when(session.isAuthenticated()).thenReturn(false);
        try {
            wrapper.handle("test", session);
        } catch (SmtpException ex) {
            assertEquals(530, ex.getResponseCode());
            assertEquals("5.7.0 Authentication required", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testAuthEnforced() throws Exception {
        when(configuration.isAuthenticationEnforced()).thenReturn(true);
        when(session.isAuthenticated()).thenReturn(true);
        //
        wrapper.handle("test", session);
        verify(command).handle(eq("test"), same(session));
    }

    @Test
    public void testAuthNotEnforced() throws Exception {
        when(configuration.isAuthenticationEnforced()).thenReturn(false);
        when(session.isConnectionSecured()).thenReturn(false, true);
        //
        wrapper.handle("test", session);
        wrapper.handle("test", session);
        verify(command, times(2)).handle(eq("test"), same(session));
    }

    @Test
    public void testGetName() throws Exception {
        when(command.getName()).thenReturn("TEST");
        Assert.assertEquals("TEST", wrapper.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        wrapper.printHelpMessage(session);
        verify(command).printHelpMessage(eq(session));
    }
}
