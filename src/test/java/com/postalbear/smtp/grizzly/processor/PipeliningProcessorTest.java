package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.command.Command;
import com.postalbear.smtp.command.CommandRegistry;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.Decoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

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
    private GrizzlySmtpSession session;
    @InjectMocks
    private PipeliningProcessor processor;

    @Before
    public void init() throws Exception {
        when(commandRegistry.getCommand(anyString())).thenReturn(command);
    }

    @Test
    public void testProcess() throws Exception {
        when(smtpInput.hasEnoughData(any(Decoder.class))).thenReturn(true, false);
        when(smtpInput.getData(any(Decoder.class))).thenReturn("TEST");
        processor.process(smtpInput, session);

        verify(command).handle(eq("TEST"), same(session));
    }

    @Test
    public void testProcessEmptyInput() throws Exception {
        when(smtpInput.hasEnoughData(any(Decoder.class))).thenReturn(false);
        processor.process(smtpInput, session);

        verify(command, never()).handle(anyString(), any(SmtpSession.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNullCommandRegistry() throws Exception {
        new PipeliningProcessor(null);
    }
}