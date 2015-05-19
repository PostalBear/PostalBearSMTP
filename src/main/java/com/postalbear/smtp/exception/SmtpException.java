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

    /**
     * Constructs instance of SmtpException.
     *
     * @param responseCode    resulting SMTP code
     * @param responseMessage diagnostic message
     */
    public SmtpException(int responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    /**
     * @return SMTP code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @return diagnostic message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return getResponseCode() + " " + getResponseMessage();
    }
}
