package com.postalbear.smtp.io;

import com.postalbear.smtp.exception.SmtpException;

import java.io.IOException;
import java.io.InputStream;

import static com.postalbear.smtp.SmtpConstants.*;

/**
 * An InputStream class that terminates the stream when it encounters a US-ASCII encoded dot CR LF byte
 * sequence immediately following a CR LF line end.
 * <p>
 * <b>Copy pasted from SubethaSMTP</b>
 */
public class DotTerminatedInputStream extends InputStream {

    private final InputStream in;
    /**
     * The last bytes returned by the {@link #read()} function. The first byte in the array contains the byte
     * returned by the penultimate read() call. The second byte in the array contains the byte returned by the
     * last read() call. EOF (-1) is not shifted into the array. It's initial value is CR LF, so the first
     * character of the stream is considered to be the first character of a line. This makes it possible to
     * receive empty data.
     */
    private final int[] lastBytes = new int[]{CR, LF};
    /**
     * The buffer which contains the bytes read from the underlying stream in advance. These bytes are not yet
     * returned by the {@link #read()} function.
     */
    private int[] nextBytes = null;
    /**
     * Indicates that the last byte - not including the terminating sequence - of the wrapped stream was
     * already returned by {@link #read()}
     */
    private boolean endReached = false;

    /**
     * A constructor for this object that takes a stream to be wrapped and a terminating character sequence.
     *
     * @param in the <code>InputStream</code> to be wrapped
     * @throws IllegalArgumentException if the terminator array is null or empty
     */
    public DotTerminatedInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (nextBytes == null) {
            //read next 3 bytes
            nextBytes = new int[]{
                    readInternal(),
                    readInternal(),
                    readInternal()
            };
        }
        if (endReached) {
            return -1;
        }
        if (lastBytesAreCrLf() && nextBytesAreDotCrLf()) {
            endReached = true;
            return -1;
        }
        int result = nextBytes[0];
        readWrappedStream();
        return result;
    }

    private boolean lastBytesAreCrLf() {
        return CR == lastBytes[0] && LF == lastBytes[1];
    }

    private boolean nextBytesAreDotCrLf() {
        return DOT == nextBytes[0] && CR == nextBytes[1] && LF == nextBytes[2];
    }

    /**
     * Shifts bytes in the buffers, reads a byte from the wrapped stream, and places it at the end of the
     * nextBytes buffer.
     */
    private void readWrappedStream() throws IOException {
        lastBytes[0] = lastBytes[1];
        lastBytes[1] = nextBytes[0];
        //
        nextBytes[0] = nextBytes[1];
        nextBytes[1] = nextBytes[2];
        nextBytes[2] = readInternal();
    }

    /**
     * read from wrapped stream and check for unexpected EOF
     *
     * @return
     * @throws IOException
     */
    private int readInternal() throws IOException {
        int result = in.read();
        if (result == -1 && !endReached) {
            // End of stream reached without seeing the terminator
            throw new SmtpException(554, "Pre-mature end of <CRLF>.<CRLF> terminated data");
        }
        return result;
    }
}
