/*
 */
package com.postalbear.smtp.auth;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.exception.SmtpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipleAuthenticationHandlerFactoryTest {

    @Mock
    private AuthenticationHandlerFactory mockedFactoryDelegate;
    @Mock
    private SmtpSession session;

    private MultipleAuthenticationHandlerFactory factory;

    @Before
    public void init() throws Exception {
        //to check that mechanisms will be converted to upper case
        when(mockedFactoryDelegate.getAuthenticationMechanisms()).thenReturn(Collections.singleton("plain"));
        factory = new MultipleAuthenticationHandlerFactory(mockedFactoryDelegate);
    }

    @Test
    public void testGetAuthenticationMechanisms() throws Exception {
        assertEquals(Collections.singleton("PLAIN"), factory.getAuthenticationMechanisms());
    }

    @Test
    public void testDumbCreate() throws Exception {
        AuthenticationHandler handler = mock(AuthenticationHandler.class);
        when(mockedFactoryDelegate.create(eq("PLAIN"), eq(session))).thenReturn(handler);
        assertSame(handler, factory.create("PLAIN", session));
    }

    @Test(expected = SmtpException.class)
    public void testCreateWithUnknownMechanism() throws Exception {
        try {
            factory.create("UNKNOWN", session);
        } catch (SmtpException ex) {
            assertEquals(504, ex.getResponseCode());
            assertEquals("5.5.4 The requested authentication mechanism is not supported", ex.getResponseMessage());
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullFactories() throws Exception {
        new MultipleAuthenticationHandlerFactory((AuthenticationHandlerFactory[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyFactories() throws Exception {
        new MultipleAuthenticationHandlerFactory(new AuthenticationHandlerFactory[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullElement() throws Exception {
        new MultipleAuthenticationHandlerFactory(new AuthenticationHandlerFactory[]{null});
    }
}
