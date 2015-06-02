package com.postalbear.smtp.grizzly.data;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.data.DataHandler;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;

/**
 * @author Grigory Fadeev.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrizzlyDataHandlerTest {

    @Mock
    private SmtpSession session;
    @InjectMocks
    private GrizzlyDataHandler dataHandler;

    @Test
    public void testKickstartData() throws Exception {
        dataHandler.kickstartData();

        verify(session).setDataHandler(eq(dataHandler));
        verify(session).sendResponse(eq(354), eq("End data with <CR><LF>.<CR><LF>"));
        verify(session).flush();
    }

    @Test
    public void testProcessData() throws Exception {
        String messageStr = "message";
        byte[] chunk = messageStr.getBytes();
        dataHandler.processData(chunk);

        ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(session).data(streamCaptor.capture());
        assertEquals(messageStr, IOUtils.toString(streamCaptor.getValue()));

        verify(session).setDataHandler(isNull(DataHandler.class));
        verify(session).sendResponse(eq(250), eq("OK"));
        verify(session).resetMailTransaction();
    }
}