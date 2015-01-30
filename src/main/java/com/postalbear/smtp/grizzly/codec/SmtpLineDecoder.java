package com.postalbear.smtp.grizzly.codec;

import com.postalbear.smtp.SmtpConstants;
import com.postalbear.smtp.exception.SmtpException;
import lombok.NonNull;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.attributes.Attribute;

import javax.annotation.concurrent.NotThreadSafe;

import static com.postalbear.smtp.SmtpConstants.*;
import static org.glassfish.grizzly.attributes.AttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * Decode grizzly Buffer (containing partially|full read SMTP line) to java String.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class SmtpLineDecoder {

    private static final int TERMINATION_BYTES_LENGTH = CRLF.length();
    private final Attribute<Integer> offset;
    private final Attribute<Integer> crPosition;
    private final Attribute<Integer> lfPosition;

    public SmtpLineDecoder() {
        offset = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpLineDecoder.offset", 0);
        crPosition = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpLineDecoder.crPosition", -1);
        lfPosition = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpLineDecoder.lfPosition", -1);
    }

    /**
     * @param storage
     * @param input
     * @return
     */
    public boolean hasCompleteLine(@NonNull Connection storage, @NonNull Buffer input) {
        int byteRead = offset.get(storage); //count of previously read bytes
        for (int i = input.position() + byteRead; i < input.limit(); i++) {
            //try to find CRLF
            if (input.get(i) == CR && !isCrSet(storage)) {
                crPosition.set(storage, i);
            } else if (input.get(i) == LF && !isLfSet(storage)) {
                lfPosition.set(storage, i);
            }

            assertCrLfSequence(storage);
            if (isLineTerminatorFound(storage)) {
                return true;
            }
            assertMaxLenghtLimitExceeded(byteRead + i);
        }
        handleIncompleteLine(storage, input);
        return false;
    }

    private boolean isCrSet(Connection storage) {
        return crPosition.get(storage) > 0;
    }

    private boolean isLfSet(Connection storage) {
        return lfPosition.get(storage) > 0;
    }

    private void assertCrLfSequence(Connection storage) {
        if (isLineTerminatorFound(storage) && crPosition.get(storage) + 1 != lfPosition.get(storage)) {
            throw new SmtpException(501, "Syntax error, bare CR or LF found,  it should be paired. see RFC5321 2.3.7");
        }
    }

    private boolean isLineTerminatorFound(Connection storage) {
        return isCrSet(storage) && isLfSet(storage);
    }

    private void assertMaxLenghtLimitExceeded(int currentPosition) {
        if (currentPosition > SMTP_LINE_MAX_SIZE) {
            throw new SmtpException(501, "Input line is too long, rejecting");
        }
    }

    private void handleIncompleteLine(Connection storage, Buffer input) {
        if (input.hasRemaining()) {
            offset.set(storage, input.remaining());
        }
    }

    /**
     * Get parsed line.
     *
     * @param storage attribute storage
     * @param input   available data
     * @return SMTP line
     */
    public String getSmtpLine(@NonNull Connection storage, @NonNull Buffer input) {
        if (!isLineTerminatorFound(storage)) {
            throw new IllegalStateException("Method is invoked when no further SmtpLine available");
        }
        int lineTerminatorIndex = crPosition.get(storage);
        int tmpLimit = input.limit();
        //
        input.limit(lineTerminatorIndex);
        String stringMessage = input.toStringContent(SmtpConstants.ASCII_CHARSET);
        //
        input.limit(tmpLimit);
        input.position(lineTerminatorIndex + TERMINATION_BYTES_LENGTH);
        release(storage);
        return stringMessage;
    }

    public void release(Connection storage) {
        offset.remove(storage);
        crPosition.remove(storage);
        lfPosition.remove(storage);
    }
}
