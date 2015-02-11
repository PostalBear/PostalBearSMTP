package com.postalbear.smtp.auth.plain;

import com.postalbear.smtp.auth.AuthStage;
import com.postalbear.smtp.exception.SmtpException;

import java.util.StringTokenizer;

/**
 * Stages of PALIN authentication mechanism.
 *
 * @author Grigory Fadeev
 */
public enum PlainAuthStage implements AuthStage<PlainAuthenticationHandler> {

    /**
     * First stage of authentication process.
     */
    INITIAL() {
        @Override
        public boolean handle(PlainAuthenticationHandler handler, String line) throws SmtpException {
            StringTokenizer stk = new StringTokenizer(line);

            stk.nextToken();//to skip AUTH keyword, not necessary to check it again
            stk.nextToken();//to skip SASL mechanism (PLAIN), not necessary to check it again
            /**
             * Let's read the RFC2554 "initial-response" parameter
             * The line could be in the form of "AUTH PLAIN <base64Secret>"
             */
            if (stk.hasMoreTokens()) {
                handler.handleSecret(stk.nextToken());
                return false;
            }
            // the client did not submit an initial response, ask for next line for processing
            handler.sendResponse(334, "OK");
            handler.setStage(REQUEST_SECRET);
            return true;
        }
    },

    /**
     * Second (optional in case of initial response) stage of authentication process.
     */
    REQUEST_SECRET() {
        @Override
        public boolean handle(PlainAuthenticationHandler handler, String line) throws SmtpException {
            StringTokenizer stk = new StringTokenizer(line);
            handler.handleSecret(stk.nextToken());
            return false;
        }
    };
}
