package com.postalbear.smtp.exception;

/**
 * Represents SMTP exceptions.
 * <b>Throwing of SmtpException leads to flush of SMTP response buffer.</b>
 *
 * @author Grigory Fadeev
 */
public class SmtpException extends RuntimeException {

    private final int responseCode;
    private final String responseMessage;

    public SmtpException(int responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public String getMessage() {
        return getResponseCode() + " " + getResponseMessage();
    }
}
