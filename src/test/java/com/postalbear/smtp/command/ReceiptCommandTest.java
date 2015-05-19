/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpServerConfiguration;
import com.postalbear.smtp.SmtpSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.internet.InternetAddress;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class ReceiptCommandTest {

    private static final String ADDRESS = "test@test.test";
    private static final String SMTP_LINE = "RCPT TO: " + ADDRESS;

    @Mock
    private SmtpServerConfiguration configuration;
    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;

    private ReceiptCommand command = new ReceiptCommand();

    @Before
    public void init() {
        when(session.getConfiguration()).thenReturn(configuration);
        //
        when(session.isMailTransactionInProgress()).thenReturn(true);
    }

    @Test
    public void testInvalidSyntax() throws Exception {
        command.handle(ADDRESS, session, input);

        verify(session).sendResponse(eq(501), eq("5.5.2 Syntax error: RCPT TO: <address>"));
        verify(session, never()).recipient(any(InternetAddress.class));
    }

    @Test
    public void testTransactionNotInProgress() throws Exception {
        when(session.isMailTransactionInProgress()).thenReturn(false);

        command.handle(SMTP_LINE, session, input);

        verify(session).sendResponse(eq(503), eq("5.5.1 Invalid sequence: need MAIL command fist"));
        verify(session, never()).recipient(any(InternetAddress.class));
    }

    @Test
    public void testTooManyRecipients() throws Exception {
        when(session.getRecipientsCount()).thenReturn(10);
        when(configuration.getMaxRecipients()).thenReturn(1);

        command.handle(SMTP_LINE, session, input);

        verify(session).sendResponse(eq(452), eq("4.5.3 Error: too many recipients"));
        verify(session, never()).recipient(any(InternetAddress.class));
    }

    @Test
    public void testLineWithAngelBrackets() throws Exception {
        String smtpLine = "RCPT TO: <" + ADDRESS + ">";

        command.handle(smtpLine, session, input);

        verify(session).recipient(eq(new InternetAddress(ADDRESS)));
        verify(session).sendResponse(eq(250), eq("recipient <" + ADDRESS + "> OK"));
        verify(session, never()).messageSize(anyInt());
    }

    @Test
    public void testPlainLine() throws Exception {
        String smtpLine = "RCPT TO:" + ADDRESS;

        command.handle(smtpLine, session, input);

        verify(session).recipient(eq(new InternetAddress(ADDRESS)));
        verify(session).sendResponse(eq(250), eq("recipient <" + ADDRESS + "> OK"));
        verify(session, never()).messageSize(anyInt());
    }

    @Test
    public void testWithInvalidAddress() throws Exception {
        String address = "(address";
        String smtpLine = "RCPT TO:" + address + "";

        command.handle(smtpLine, session, input);

        verify(session).sendResponse(eq(553), eq("5.1.3 Syntax error: recipient address <" + address + "> is invalid"));
        verify(session, never()).recipient(any(InternetAddress.class));
    }

    @Test
    public void testUnsuportedParameter() throws Exception {
        String smtpLine = "RCPT TO:" + ADDRESS + " PARAM=1";

        command.handle(smtpLine, session, input);

        verify(session).sendResponse(eq(555), eq("Command parameter \"PARAM=1\" not implemented"));
        verify(session, never()).recipient(any(InternetAddress.class));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("RCPT", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-RCPT command, to specify recipient of e-mail, separate invocation for each recipient should be done."));
        verify(session).sendResponseAsString(eq("214-RCPT TO: <recipient> [ <parameters> ]"));
        verify(session).sendResponseAsString(eq("214-Currently command does not support parameters"));
    }
}
