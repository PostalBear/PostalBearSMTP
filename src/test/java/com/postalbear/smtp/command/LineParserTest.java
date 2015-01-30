/*
 */
package com.postalbear.smtp.command;

import com.postalbear.smtp.exception.SmtpException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Grigory Fadeev
 */
@RunWith(Theories.class)
public class LineParserTest {

    @DataPoints
    //list of valid addresses from rfc3696#section-3
    public static String[] ADDRESSES = {
            "Abc\\@def@example.com",
            "Fred\\ Bloggs@example.com",
            "Joe.\\\\Blow@example.com",
            "\"Abc@def\"@example.com",
            "\"Fred Bloggs\"@example.com",
            "\"Fred(comment<>)Bloggs\"@example.com",
            "user+mailbox@example.com",
            "customer/department=shipping@example.com",
            "$A12345@example.com",
            "!def!xyz%abc@example.com",
            "_somename@example.com",
            "\"! # $ % & ' * + - / = ?  ^ _ ` . { | } ~\"\\ @test.test"
    };

    private final String PARAMETER = "parameter=value";
    private final LineParser parser = new LineParser();

    @Theory
    public void testPlainLine(String address) throws Exception {
        ParsingResult result = parser.parseLine(address);
        Assert.assertEquals(address, result.getEmailAddress());
        Assert.assertEquals("", result.getRestOfLine());
    }

    @Theory
    public void testPlainLineWithParams(String address) throws Exception {

        String smtpLine = address + " " + PARAMETER;
        ParsingResult result = parser.parseLine(smtpLine);
        Assert.assertEquals(address, result.getEmailAddress());
        Assert.assertEquals(PARAMETER, result.getRestOfLine());
    }

    @Theory
    public void testWithAngelBrackets(String address) throws Exception {
        String smtpLine = "<" + address + ">";
        ParsingResult result = parser.parseLine(smtpLine);
        Assert.assertEquals(address, result.getEmailAddress());
        Assert.assertEquals("", result.getRestOfLine());
    }

    @Theory
    public void testWithAngelBracketsAndParams(String address) throws Exception {
        String smtpLine = "<" + address + "> " + PARAMETER;
        ParsingResult result = parser.parseLine(smtpLine);
        Assert.assertEquals(address, result.getEmailAddress());
        Assert.assertEquals(PARAMETER, result.getRestOfLine());
    }

    @Test
    public void testMailGroup() throws Exception {
        String group = "Undisclosed Recipient:;";
        //Mailing group is not allowed by RFC 2821/5321 as a parameter for MAIL and RCPT commands
        //@see http://tools.ietf.org/html/rfc5321#section-2.2 for additional details
        ParsingResult result = parser.parseLine(group);
        Assert.assertEquals("Undisclosed", result.getEmailAddress());
        Assert.assertEquals("Recipient:;", result.getRestOfLine());
    }

    @Test
    public void testWithMissedAngelBracket() throws Exception {
        String smtpLine = "<test@test.test";
        try {
            parser.parseLine(smtpLine);
            fail("SmtpException expected");
        } catch (SmtpException ex) {
            assertEquals(501, ex.getResponseCode());
            assertEquals("5.5.2 Syntax error: closing '>' not found", ex.getResponseMessage());
        }
    }
}
