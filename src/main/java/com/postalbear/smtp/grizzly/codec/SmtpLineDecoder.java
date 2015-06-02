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
 * This decoder is used to get complete SMTP lines from input.
 * SMTP line is a sequence of characters no longer than {@value com.postalbear.smtp.SmtpConstants#SMTP_LINE_MAX_SIZE}
 * and terminated with CRLF.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class SmtpLineDecoder implements Decoder<String> {

    private static final SmtpLineDecoder INSTANCE = new SmtpLineDecoder();

    private final Attribute<Integer> offset;
    private final Attribute<Integer> crPosition;
    private final Attribute<Integer> lfPosition;

    /**
     * @return single instance of SmtpLineDecoder class
     */
    public static SmtpLineDecoder getInstance() {
        return INSTANCE;
    }

    /**
     * Constructs SmtpLineDecoder instance.
     */
    private SmtpLineDecoder() {
        offset = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpLineDecoder.offset", 0);
        crPosition = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpLineDecoder.crPosition", -1);
        lfPosition = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpLineDecoder.lfPosition", -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasEnoughData(@NonNull Buffer input, @NonNull Connection storage) {
        int byteRead = offset.get(storage);
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
     * {@inheritDoc}
     */
    @Override
    public String getData(@NonNull Buffer input, @NonNull Connection storage) {
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
        input.position(lineTerminatorIndex + CRLF_LENGHT);
        release(storage);
        return stringMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release(Connection storage) {
        offset.remove(storage);
        crPosition.remove(storage);
        lfPosition.remove(storage);
    }
}
