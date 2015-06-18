package com.postalbear.smtp.grizzly.processor;

import com.postalbear.smtp.data.DataHandler;
import com.postalbear.smtp.grizzly.GrizzlySmtpSession;
import com.postalbear.smtp.grizzly.SmtpInput;
import com.postalbear.smtp.grizzly.codec.MessageDecoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageReceivingProcessorTest {

    @Mock
    private SmtpInput smtpInput;
    @Mock
    private GrizzlySmtpSession session;
    @Mock
    private SmtpProcessor nextProcessor;
    @Mock
    private DataHandler dataHandler;
    @InjectMocks
    private MessageReceivingProcessor messageReceivingProcessor;

    @Before
    public void setUp() throws Exception {
        when(session.getDataHandler()).thenReturn(dataHandler);
    }

    @Test
    public void testProcessIncompleteMessage() throws Exception {
        messageReceivingProcessor.process(smtpInput, session);

        verify(dataHandler, never()).processData(any(byte[].class));
        verify(nextProcessor).process(eq(smtpInput), eq(session));
    }

    @Test
    public void testProcessSuccessfully() throws Exception {
        byte[] expectedDataChunk = new byte[0];
        when(smtpInput.hasEnoughData(any(MessageDecoder.class))).thenReturn(true, false);
        when(smtpInput.getData(any(MessageDecoder.class))).thenReturn(expectedDataChunk);

        messageReceivingProcessor.process(smtpInput, session);
        verify(dataHandler).processData(same(expectedDataChunk));
        verify(nextProcessor).process(eq(smtpInput), eq(session));
    }
}