package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.command.Command;
import com.postalbear.smtp.command.CommandRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class PipeliningProcessorTest {

    @Mock
    private CommandRegistry commandRegistry;
    @Mock
    private Command command;
    @Mock
    private SmtpInput smtpInput;
    @Mock
    private SmtpSession session;
    @InjectMocks
    private PipeliningProcessor processor;

    @Before
    public void init() throws Exception {
        when(commandRegistry.getCommand(anyString())).thenReturn(command);
    }

    @Test
    public void testProcess() throws Exception {
        when(smtpInput.getSmtpLine()).thenReturn("TEST");
        processor.process(smtpInput, session);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCommandRegistry() throws Exception {
        new PipeliningProcessor(null);
    }
}