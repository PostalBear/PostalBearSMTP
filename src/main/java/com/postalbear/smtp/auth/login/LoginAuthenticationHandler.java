/*
 */
package com.postalbear.smtp.auth.login;

import com.postalbear.smtp.SmtpConstants;
import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AbstractAuthenticationHandler;
import com.postalbear.smtp.auth.AuthState;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.io.SmtpLineReader;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Base64;
import java.util.StringTokenizer;

import static com.postalbear.smtp.SmtpConstants.UTF8_CHARSET;

/**
 * Implements LOGIN authentication mechanism.
 * See RFC4954
 *
 * @author Grigory
 */
@NotThreadSafe
public class LoginAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final String USERNAME_KEYWORD_ENCODED
            = Base64.getEncoder().encodeToString("Username:".getBytes(SmtpConstants.ASCII_CHARSET));
    private static final String PASSWORD_KEYWORD_ENCODED
            = Base64.getEncoder().encodeToString("Password:".getBytes(SmtpConstants.ASCII_CHARSET));

    private String username;
    private String password;

    public LoginAuthenticationHandler(SmtpSession session, SmtpLineReader inputLineReader, CredentialsValidator validator) {
        super(session, inputLineReader, validator);
        setState(new InitialAuthState());
    }

    private void readUsername(String line) throws SmtpException {
        try {
            username = new String(Base64.getDecoder().decode(line), UTF8_CHARSET);
        } catch (IllegalArgumentException ex) {
            throw new SmtpException(501, "5.5.2 Invalid command argument: Username - not a valid Base64 string");
        }
    }

    private void readPassword(String line) throws SmtpException {
        try {
            password = new String(Base64.getDecoder().decode(line), UTF8_CHARSET);
        } catch (IllegalArgumentException ex) {
            throw new SmtpException(501, "5.5.2 Invalid command argument: Password - not a valid Base64 string");
        }
    }

    /**
     * First stage of authentication process.
     * Client might submit initial response within AUTH command
     * in such case credential validation will be performed immediately
     * otherwise pass processing of next lines to @see ReadSecretState.
     */
    private class InitialAuthState implements AuthState {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean handle(String line) throws SmtpException {
            StringTokenizer stk = new StringTokenizer(line);

            stk.nextToken();//to skip AUTH keyword, not necessary to check it again
            stk.nextToken();//to skip SASL mechanism (LOGIN), not necessary to check it again

            if (stk.hasMoreTokens()) {
                readUsername(stk.nextToken());
                sendResponse(334, PASSWORD_KEYWORD_ENCODED);
                setState(new ReadPasswordState());
            } else {
                sendResponse(334, USERNAME_KEYWORD_ENCODED);
                setState(new ReadUsernameState());
            }
            return true;
        }
    }

    private class ReadUsernameState implements AuthState {

        @Override
        public boolean handle(String line) throws SmtpException {
            readUsername(line);
            sendResponse(334, PASSWORD_KEYWORD_ENCODED);
            setState(new ReadPasswordState());
            return true;
        }
    }

    private class ReadPasswordState implements AuthState {

        @Override
        public boolean handle(String line) throws SmtpException {
            readPassword(line);
            validateCredentials(username, password);
            return false;
        }
    }
}
