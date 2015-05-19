/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.command.param.SizeParameterHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.internet.InternetAddress;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * @author Grigory Fadeev
 */
@RunWith(MockitoJUnitRunner.class)
public class MailCommandTest {

    private static final String ADDRESS = "test@test.test";

    @Mock
    private SmtpSession session;
    @Mock
    private SmtpInput input;
    @Mock
    private SizeParameterHandler sizeHandler;
    private MailCommand command;

    @Before
    public void init() {
        command = new MailCommand(sizeHandler);
        when(session.isMailTransactionInProgress()).thenReturn(false);
    }

    @Test
    public void testSyntaxIncorrect() throws Exception {
        command.handle(ADDRESS, session, input);

        verify(session).sendResponse(eq(501), eq("5.5.2 Syntax error: MAIL FROM: <address> [parameters]"));
    }

    @Test
    public void testTransactionInProgress() throws Exception {
        when(session.isMailTransactionInProgress()).thenReturn(true);

        command.handle("MAIL FROM:" + ADDRESS, session, input);

        verify(session).sendResponse(eq(503), eq("5.5.1 Invalid sequence: sender already specified."));
    }

    @Test
    public void testParseLineWithAngelBrackets() throws Exception {
        String smtpLine = "MAIL FROM: <" + ADDRESS + ">";

        command.handle(smtpLine, session, input);

        verify(session).from(eq(new InternetAddress(ADDRESS)));
        verify(session).sendResponse(eq(250), eq("sender <" + ADDRESS + "> OK"));
        verify(session, never()).messageSize(anyInt());
    }

    @Test
    public void testParseLinePlain() throws Exception {
        String smtpLine = "MAIL FROM:" + ADDRESS;

        command.handle(smtpLine, session, input);

        verify(session).from(eq(new InternetAddress(ADDRESS)));
        verify(session).sendResponse(eq(250), eq("sender <" + ADDRESS + "> OK"));
        verify(session, never()).messageSize(anyInt());
    }

    @Test
    public void testInvalidAddress() throws Exception {
        String address = "(address";
        String smtpLine = "MAIL FROM:" + address + "";

        command.handle(smtpLine, session, input);

        verify(session).sendResponse(eq(553), eq("5.1.7 Syntax error: sender address <" + address + "> is invalid"));
    }

    @Test
    public void testUnsuportedParameter() throws Exception {
        String smtpLine = "MAIL FROM:" + ADDRESS + " AUTH=<>";

        command.handle(smtpLine, session, input);

        verify(session).sendResponse(eq(504), eq("5.5.4 Command parameter \"AUTH=<>\" not implemented"));
    }

    @Test
    public void testSizeParameter() throws Exception {
        String smtpLine = "MAIL FROM:" + ADDRESS + " SIZE=100";
        when(sizeHandler.match(eq("size=100"))).thenReturn(true);

        command.handle(smtpLine, session, input);

        verify(session).from(eq(new InternetAddress(ADDRESS)));
        verify(session).sendResponse(eq(250), eq("sender <" + ADDRESS + "> OK"));
        verify(sizeHandler).match(eq("size=100"));
        verify(sizeHandler).handleLine(same(session), eq("size=100"));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("MAIL", command.getName());
    }

    @Test
    public void testPrintHelpMessage() throws Exception {
        command.printHelpMessage(session);
        verify(session).sendResponseAsString(eq("214-MAIL command, to specify sender of e-mail."));
        verify(session).sendResponseAsString(eq("214-MAIL FROM: <sender> [ <parameters> ]"));
        verify(session).sendResponseAsString(eq("214-List of supported parameters:"));
        verify(session).sendResponseAsString(eq("214-  SIZE=to inform server about size of e-mail (in bytes)"));
    }
}
