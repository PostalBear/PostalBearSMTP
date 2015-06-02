package com.postalbear.smtp.grizzly.codec;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.attributes.Attribute;

import static com.postalbear.smtp.SmtpConstants.*;
import static org.glassfish.grizzly.attributes.AttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * Decoder to read mail message from SmtpInput.
 *
 * @author Grigory Fadeev.
 */
public class MessageDecoder implements Decoder<byte[]> {

    private static final MessageDecoder INSTANCE = new MessageDecoder();

    private final Attribute<Integer> offset;
    private final Attribute<Integer> endOfMessage;

    /**
     * @return single instance of SmtpLineDecoder class
     */
    public static MessageDecoder getInstance() {
        return INSTANCE;
    }

    /**
     * Constructs instance of MessageDecoder.
     */
    private MessageDecoder() {
        offset = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("MessageDecoder.offset", 0);
        endOfMessage = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("MessageDecoder.endOfMessage", -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasEnoughData(Buffer input, Connection storage) {
        int byteRead = offset.get(storage);
        for (int i = input.position() + byteRead; i < input.limit(); i++) {
            if (input.get(i) == LF) {
                if (isEndOfMessage(i, input, storage)) {
                    endOfMessage.set(storage, i + 1 - DOTCRLF_LENGHT);
                    return true;
                }
            }
        }
        handleIncompleteMessage(storage, input);
        return false;
    }

    private boolean isEndOfMessage(final int position, Buffer input, Connection storage) {
        if (position > CRLF_LENGHT + DOTCRLF_LENGHT) {
            int cursor = position;
            return input.get(cursor) == LF
                    && input.get(--cursor) == CR
                    && input.get(--cursor) == DOT
                    && input.get(--cursor) == LF
                    && input.get(--cursor) == CR;
        }
        return false;
    }

    private void handleIncompleteMessage(Connection storage, Buffer input) {
        if (input.hasRemaining()) {
            offset.set(storage, input.remaining());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getData(Buffer input, Connection storage) {
        if (endOfMessage.get(storage) < 0) {
            throw new IllegalStateException("Method is invoked when message is incomplete");
        }

        final int originalLimit = input.limit();
        //
        int endOfMessageIndex = endOfMessage.get(storage);
        input.limit(endOfMessageIndex);

        final byte[] result = new byte[input.limit() - input.position()];
        input.get(result);
        //
        input.limit(originalLimit);
        input.position(endOfMessageIndex + DOTCRLF_LENGHT);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release(Connection storage) {
        offset.remove(storage);
        endOfMessage.remove(storage);
    }
}
