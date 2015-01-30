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
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    /**
     * The maximum total length of a text line including the <CRLF> is 1000 characters (not counting the
     * leading dot duplicated for transparency).
     * This number may be increased by the use of SMTP Service Extensions.
     */
    public static final int SMTP_LINE_MAX_SIZE = 998; //minus 2 bytes for CRLF
    /**
     *
     */
    public static final String COMMAND_SEPARATOR = StringUtils.SPACE;
    /**
     * SMTP line ending.
     */
    public static final byte DOT = (byte) '.';
    public static final byte CR = (byte) '\r';
    public static final byte LF = (byte) '\n';
    public static final String CRLF = "\r\n";

    private SmtpConstants() {
    }
}
