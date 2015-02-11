package com.postalbear.smtp.auth.plain;

import com.postalbear.smtp.SmtpSession;
import com.postalbear.smtp.auth.AbstractAuthenticationHandler;
import com.postalbear.smtp.auth.CredentialsValidator;
import com.postalbear.smtp.exception.SmtpException;
import com.postalbear.smtp.io.SmtpLineReader;
import lombok.NonNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Base64;

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
public class PlainAuthenticationHandler extends AbstractAuthenticationHandler<PlainAuthStage> {

    private static final byte NUL = (byte) 0;
    //
    private final CredentialsValidator validator;

    /**
     * Constructs instance of PlainAuthenticationHandler.
     *
     * @param session   for which authentication is started
     * @param reader    to get more data from the client
     * @param validator to validate credentials
     */
    public PlainAuthenticationHandler(SmtpSession session, SmtpLineReader reader, @NonNull CredentialsValidator validator) {
        super(session, reader, PlainAuthStage.INITIAL);
        this.validator = validator;
    }

    /**
     * Method intended to parse secret receiving from client and proceed with authentication.
     *
     * @param secret should be Base64 encoded string
     * @throws SmtpException indicates about error during authentication process
     */
    void handleSecret(String secret) throws SmtpException {
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

    byte[] decodeSecret(String secret) throws SmtpException {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new SmtpException(501, "5.5.2 Invalid command argument, not a valid Base64 string");
        }
    }

    String getAuthentication(byte[] decodedSecret, int endOfAuthorization, int endOfAuthentication) {
        return new String(decodedSecret,
                endOfAuthorization + 1,
                endOfAuthentication - (endOfAuthorization + 1),
                UTF8_CHARSET);
    }

    String getPassword(byte[] decodedSecret, int endOfAuthentication) {
        return new String(decodedSecret,
                endOfAuthentication + 1,
                decodedSecret.length - (endOfAuthentication + 1),
                UTF8_CHARSET);
    }

    void validateCredentials(String authentication, String password) throws SmtpException {
        if (!validator.validateCredentials(authentication, password)) {
            throw new SmtpException(535, "5.7.8 Authentication failure, invalid credentials");
        }
        completeAuthentication();
    }
}
