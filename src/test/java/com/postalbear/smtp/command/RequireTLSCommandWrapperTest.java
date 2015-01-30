/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class RequireTLSCommandWrapperTest {

    @Mock
    private Command command;
    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private RequireTLSCommandWrapper wrapper;

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
        wrapper = new RequireTLSCommandWrapper(command);
    }

    @Test(expected = SmtpException.class)
    public void testPlainConnectionAndTLSEnforced() throws Exception {
        when(configuration.isStartTlsEnforced()).thenReturn(true);
        when(session.isConnectionSecured()).thenReturn(false);
        try {
            wrapper.handle("test", session, input);
        } catch (SmtpException ex) {
            assertEquals(530, ex.getResponseCode());
            assertEquals("5.7.0 Must issue a STARTTLS command first", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test
    public void testSecuredConnectionAndTLSEnforced() throws Exception {
        when(configuration.isStartTlsEnforced()).thenReturn(true);
        when(session.isConnectionSecured()).thenReturn(true);
        //
        wrapper.handle("test", session, input);
        verify(command).handle(eq("test"), same(session), eq(input));
    }

    @Test
    public void testTLSNotEnforced() throws Exception {
        when(configuration.isStartTlsEnforced()).thenReturn(false);
        when(session.isConnectionSecured()).thenReturn(false, true);
        //
        wrapper.handle("test", session, input);
        wrapper.handle("test", session, input);
        verify(command, times(2)).handle(eq("test"), same(session), eq(input));
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
