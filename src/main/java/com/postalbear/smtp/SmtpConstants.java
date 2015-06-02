package com.postalbear.smtp;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;

/**
 * Few useful SMTP constants.
 * <p>
 * See RFC5321 and RFC5322 for additional details.
 *
 * @author Grigory Fadeev
 */
public final class SmtpConstants {

    /**
     * In accordance with RFC5321 all SMTP commands should be in ASCII encoding.
     */
    public static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");
    /**
     * Represents UTF-8 charset.
     */
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    /**
     * Represents '.' (DOT) symbol.
     */
    public static final byte DOT = (byte) '.';
    /**
     * Represents '\r' (CR) symbol.
     */
    public static final byte CR = (byte) '\r';
    /**
     * Represents '\n' (LF) symbol.
     */
    public static final byte LF = (byte) '\n';
    /**
     * Represents CRLF sequence, which is terminator sequence in SMTP.
     */
    public static final String CRLF = "\r\n";
    /**
     * Length of CRLF sequence.
     */
    public static final int CRLF_LENGHT = 2;
    /**
     * Lenght of Dot followed by CRLF sequence.
     */
    public static final int DOTCRLF_LENGHT = 3;
    /**
     * The maximum total length of a text line including the <CRLF> is 1000 characters
     * (not counting the leading dot duplicated for transparency).
     * This number may be increased by the use of SMTP Service Extensions.
     */
    public static final int SMTP_LINE_MAX_SIZE = 1000 - CRLF_LENGHT;
    /**
     * Standard separator which is used to separate commands and parameters.
     */
    public static final String COMMAND_SEPARATOR = StringUtils.SPACE;

    private SmtpConstants() {
    }
}
