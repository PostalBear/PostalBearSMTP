package com.postalbear.smtp.auth.plain;

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
import static org.apache.commons.lang3.ArrayUtils.INDEX_NOT_FOUND;
import static org.apache.commons.lang3.ArrayUtils.indexOf;

/**
 * Implements PLAIN authentication mechanism.
 * See RFC4954
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class PlainAuthenticationHandler extends AbstractAuthenticationHandler {

    /*see RFC4616 for more details*/
    private static final byte NUL = (byte) 0;

    /**
     * Constructs new PlainAuthenticationHandler instance.
     *
     * @param session
     * @param validator to check credentials
     */
    public PlainAuthenticationHandler(SmtpSession session, SmtpLineReader inputLineReader, CredentialsValidator validator) {
        super(session, inputLineReader, validator);
        setState(new InitialAuthState());
    }

    /**
     * Method intended to parse secret receiving from client and proceed with authentication.
     *
     * @param secret should be Base64 encoded string
     * @throws SmtpException indicates about error during authentication process
     */
    private void handleSecret(String secret) throws SmtpException {
        byte[] decodedSecret = decodeSecret(secret);
        /*
         * RFC4616: The client presents the authorization identity (identity to act as), 
         * followed by a NUL (U+0000) character, 
         * followed by the authentication identity (identity whose password will be used),
         * followed by a NUL (U+0000) character, followed by the clear-text password.
         */
        int endOfAuthorization = indexOf(decodedSecret, NUL);
        int endOfAuthentication = indexOf(decodedSecret, NUL, endOfAuthorization + 1);
        if (!(endOfAuthentication > endOfAuthorization && endOfAuthorization > INDEX_NOT_FOUND)) {
            throw new SmtpException(501, "5.5.2 Invalid command argument, does not contain NUL");
        }
        //
        String authentication = getAuthentication(decodedSecret, endOfAuthorization, endOfAuthentication);
        String password = getPassword(decodedSecret, endOfAuthentication);

        validateCredentials(authentication, password);
    }

    private byte[] decodeSecret(String secret) throws SmtpException {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new SmtpException(501, "5.5.2 Invalid command argument, not a valid Base64 string");
        }
    }

    private String getAuthentication(byte[] decodedSecret, int endOfAuthorization, int endOfAuthentication) {
        return new String(decodedSecret,
                endOfAuthorization + 1,
                endOfAuthentication - (endOfAuthorization + 1),
                UTF8_CHARSET);
    }

    private String getPassword(byte[] decodedSecret, int endOfAuthentication) {
        return new String(decodedSecret,
                endOfAuthentication + 1,
                decodedSecret.length - (endOfAuthentication + 1),
                UTF8_CHARSET);
    }

    /**
     * First step of authentication process.
     * Client might submit initial response within AUTH command,
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
            stk.nextToken();//to skip SASL mechanism (PLAIN), not necessary to check it again
            /**
             * Let's read the RFC2554 "initial-response" parameter
             * The line could be in the form of "AUTH PLAIN <base64Secret>"
             */
            if (stk.hasMoreTokens()) {
                handleSecret(stk.nextToken());
                return false;
            }
            // the client did not submit an initial response, ask for next line for processing
            sendResponse(334, "OK");
            setState(new ReadSecretState());
            return true;
        }
    }

    /**
     * Second and last step of authentication process.
     * If client did not submit secret during AUTH command, it should be done after receiving 334 OK from the server.
     */
    private class ReadSecretState implements AuthState {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean handle(String line) throws SmtpException {
            StringTokenizer stk = new StringTokenizer(line);
            handleSecret(stk.nextToken());
            return false;
        }
    }
}
