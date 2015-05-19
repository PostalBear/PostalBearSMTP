/*
 */
package com.postalbear.smtp.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipleAuthenticationHandlerFactoryTest {

    @Mock
    private AuthenticationHandlerFactory mockedFactoryDelegate;

    private MultipleAuthenticationHandlerFactory factory;

    @Before
    public void init() {
        //to check that mechanisms will be converted to upper case
        when(mockedFactoryDelegate.getAuthenticationMechanisms()).thenReturn(Collections.singleton("plain"));
        factory = new MultipleAuthenticationHandlerFactory(mockedFactoryDelegate);
    }

    @Test
    public void testGetAuthenticationMechanisms() {
        assertEquals(Collections.singleton("PLAIN"), factory.getAuthenticationMechanisms());
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
