package com.postalbear.smtp.command;

/**
 * DTO contains parsed SMTP line for MAIL | RCPT commands.
 *
 * @author Grigory Fadeev
 */
public class ParsingResult {

    private String emailAddress;
    private String restOfLine = "";

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setRestOfLine(String restOfLine) {
        this.restOfLine = restOfLine;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getRestOfLine() {
        return restOfLine;
    }
}
