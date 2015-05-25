package com.postalbear.smtp.grizzly.auth;

import com.postalbear.smtp.auth.AuthenticationHandlerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test does not check create method intentionally.
 * Check of resulting object is subject for integration test.
 *
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrizzlyAuthenticationHandlerFactoryTest {

    @Mock
    private AuthenticationHandlerFactory delegatingFactory;
    @InjectMocks
    private GrizzlyAuthenticationHandlerFactory factory;

    @Test
    public void testGetAuthenticationMechanisms() throws Exception {
        Set<String> mechanisms = Collections.singleton("LOGIN");
        when(delegatingFactory.getAuthenticationMechanisms()).thenReturn(mechanisms);
        assertEquals(mechanisms, factory.getAuthenticationMechanisms());
    }
}