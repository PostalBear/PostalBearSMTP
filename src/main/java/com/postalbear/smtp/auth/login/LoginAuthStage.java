package com.postalbear.smtp.auth.login;

import com.postalbear.smtp.auth.AuthStage;
import com.postalbear.smtp.exception.SmtpException;

import java.util.Base64;
import java.util.StringTokenizer;

import static com.postalbear.smtp.SmtpConstants.ASCII_CHARSET;

/**
 * Stages of LOGIN authentication mechanism.
 *
 * @author Grigory Fadeev
 */
public enum LoginAuthStage implements AuthStage<LoginAuthenticationHandler> {

    /**
     * First stage of authentication process.
     */
    INITIAL() {
        @Override
        public boolean handle(LoginAuthenticationHandler handler, String line) throws SmtpException {
            StringTokenizer stk = new StringTokenizer(line);

            stk.nextToken();//to skip AUTH keyword, not necessary to check it again
            stk.nextToken();//to skip SASL mechanism (LOGIN), not necessary to check it again

            if (!stk.hasMoreTokens()) {
                handler.sendResponse(334, USERNAME_KEYWORD_ENCODED);
                handler.setStage(REQUEST_USERNAME);
            } else {
                handler.readUsername(stk.nextToken());
                handler.sendResponse(334, PASSWORD_KEYWORD_ENCODED);
                handler.setStage(REQUEST_PASSWORD);
            }
            return true;
        }
    },

    /**
     * Second (optional in case of initial response) stage of authentication.
     * Server requests for username.
     */
    REQUEST_USERNAME() {
        @Override
        public boolean handle(LoginAuthenticationHandler handler, String line) throws SmtpException {
            handler.readUsername(line);
            handler.sendResponse(334, PASSWORD_KEYWORD_ENCODED);
            handler.setStage(REQUEST_PASSWORD);
            return true;
        }
    },

    /**
     * Third (last) stage of authentication.
     * Server requests for password.
     */
    REQUEST_PASSWORD() {
        @Override
        public boolean handle(LoginAuthenticationHandler handler, String line) throws SmtpException {
            handler.readPassword(line);
            handler.validateCredentials();
            return false;
        }
    };

    private static final String USERNAME_KEYWORD_ENCODED = encodeString("Username:");
    private static final String PASSWORD_KEYWORD_ENCODED = encodeString("Password:");

    private static String encodeString(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(ASCII_CHARSET));
    }
}
