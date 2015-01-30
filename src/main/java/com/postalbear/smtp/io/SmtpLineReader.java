package com.postalbear.smtp.io;

import java.io.IOException;

/**
 * Implementation provides could be used to read SMTP lines received from client.
 *
 * @author Grigory Fadeev
 */
public interface SmtpLineReader {

    /**
     * Read SMTP line.
     *
     * @return
     * @throws java.io.IOException
     */
    String readLine() throws IOException;
}
