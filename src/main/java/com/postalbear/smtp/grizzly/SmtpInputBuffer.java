package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.grizzly.codec.Decoder;
import lombok.NonNull;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.ReadResult;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.memory.BuffersBuffer;
import org.glassfish.grizzly.memory.CompositeBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.io.InputStream;

import static org.glassfish.grizzly.attributes.AttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * Hack for Pipelining support.
 * <p>
 * By standard client might send next transaction immediately after CRLF.CRLF for DATA command.
 * So somehow we need to push back this portion of data for further processing by SmtpFilter.
 * This class is introduced especially for this purpose, and for this moment i don't have better idea.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class SmtpInputBuffer implements SmtpInput {

    private static final Attribute<SmtpInputBuffer> SMTP_INPUT_BUFFER =
            DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpInputBuffer");

    private FilterChainContext context;
    private CompositeBuffer buffer;

    /**
     * Returns SmtpInputBuffer instance associated with Connection.
     * If no SmtpInputBuffer instance is yet available, create new one.
     *
     * @param ctx to access current connection
     * @return SmtpInputBuffer instance
     */
    public static SmtpInputBuffer getSmtpInputBuffer(@NonNull FilterChainContext ctx) {
        SmtpInputBuffer instance = SMTP_INPUT_BUFFER.get(ctx.getConnection());
        if (instance == null) {
            instance = new SmtpInputBuffer();
            SMTP_INPUT_BUFFER.set(ctx.getConnection(), instance);
        }
        instance.context = ctx;
        return instance;
    }

    /**
     * Add chunk of data to buffer.
     *
     * @param dataChunk to add
     */
    public void appendMessage(@NonNull Buffer dataChunk) {
        if (buffer == null) {
            buffer = BuffersBuffer.create(context.getMemoryManager());
        }
        buffer.append(dataChunk);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return buffer == null || !buffer.hasRemaining();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasEnoughData(Decoder<?> decoder) {
        return buffer != null && decoder.hasEnoughData(context.getConnection(), buffer);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getData(Decoder<T> decoder) {
        T result = decoder.getData(context.getConnection(), buffer);
        if (isEmpty()) {
            release(decoder);
        }
        return result;
    }

    private void release(Decoder<?> decoder) {
        decoder.release(context.getConnection());
        buffer = null;
    }

    @Deprecated
    private void fillBufferBlocking() throws IOException {
        ReadResult readResult = context.read();
        Buffer dataChunk = (Buffer) readResult.getMessage();
        readResult.recycle();
        //append data chunk
        appendMessage(dataChunk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() {
        return new BlockingInputStream();
    }

    private class BlockingInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            if (isEmpty()) {
                //wait until some data arrive 
                fillBufferBlocking();
                //check that Buffer contains data to read
                if (isEmpty()) {
                    return -1;
                }
            }
            return buffer.get();
        }
    }
}
