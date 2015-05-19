/*
 */
package com.postalbear.smtp.auth.login;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AbstractAuthenticationHandler;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Base64;

import static com.postalbear.smtp.SmtpConstants.UTF8_CHARSET;

/**
 * Implements LOGIN authentication mechanism.
 * See RFC4954
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class LoginAuthenticationHandler extends AbstractAuthenticationHandler<LoginAuthStage> {

    private final CredentialsValidator validator;
    private String username;
    private String password;

    /**
     * Constructs instance of LoginAuthenticationHandler.
     *
     * @param session   to authenticate
     * @param validator to validate credentials
     */
    public LoginAuthenticationHandler(SmtpSession session, @NonNull CredentialsValidator validator) {
        super(session, LoginAuthStage.INITIAL);
        this.validator = validator;
    }

    void readUsername(String line) throws SmtpException {
        try {
            username = new String(Base64.getDecoder().decode(line), UTF8_CHARSET);
        } catch (IllegalArgumentException ex) {
            throw new SmtpException(501, "5.5.2 Invalid command argument: Username - not a valid Base64 string");
        }
    }

    void readPassword(String line) throws SmtpException {
        try {
            password = new String(Base64.getDecoder().decode(line), UTF8_CHARSET);
        } catch (IllegalArgumentException ex) {
            throw new SmtpException(501, "5.5.2 Invalid command argument: Password - not a valid Base64 string");
        }
    }

    void validateCredentials() throws SmtpException {
        if (!validator.validateCredentials(username, password)) {
            throw new SmtpException(535, "5.7.8 Authentication failure, invalid credentials");
        }
        markAsSuccessful();
    }
}
