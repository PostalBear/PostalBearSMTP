package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.SmtpInput;
import com.postalbear.smtp.grizzly.codec.SmtpLineDecoder;
import com.postalbear.smtp.io.SmtpLineReader;
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
 * By standard client might send next transaction immediately after CRLF.CRLF for DATA command.
 * So somehow we need to push back this portion of data for further processing by SmtpFilter.
 * This class is introduced especially for this purpose, and for this moment i don't have better idea.
 *
 * @author Grigory Fadeev
 */
@NotThreadSafe
public class SmtpInputBuffer implements SmtpInput {

    private static final Attribute<SmtpInputBuffer> SMTP_INPUT_BUFFER = DEFAULT_ATTRIBUTE_BUILDER.createAttribute("SmtpInputBuffer");

    private final SmtpLineDecoder decoder = new SmtpLineDecoder();
    private final BlockingReader blockingLineReader = new BlockingReader();
    private FilterChainContext context;
    private CompositeBuffer buffer;

    /**
     * @param ctx
     * @return
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

    public void appendDataChunk(@NonNull Buffer dataChunk) {
        if (buffer == null) {
            buffer = BuffersBuffer.create(context.getMemoryManager());
        }
        buffer.append(dataChunk);
    }

    public boolean hasNextSmtpLine() {
        return decoder.hasCompleteLine(context.getConnection(), buffer);
    }

    public String getSmtpLine() {
        return decoder.getSmtpLine(context.getConnection(), buffer);
    }

    public boolean isEmpty() {
        return !buffer.hasRemaining();
    }

    public void release() {
        decoder.release(context.getConnection());
        buffer = null;
    }

    private void fillBufferBlocking() throws IOException {
        ReadResult readResult = context.read();
        Buffer dataChunk = (Buffer) readResult.getMessage();
        readResult.recycle();
        //append data chunk
        buffer.append(dataChunk);
    }

    @Override
    public SmtpLineReader getSmtpLineReader() {
        return blockingLineReader;
    }

    private class BlockingReader implements SmtpLineReader {

        @Override
        public String readLine() throws IOException {
            while (!hasNextSmtpLine()) {
                fillBufferBlocking();
            }
            return getSmtpLine();
        }
    }

    @Override
    public InputStream getInputStream() {
        return new BlockingInputStream();
    }

    private class BlockingInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            if (!buffer.hasRemaining()) {
                //wait until some data arrive 
                fillBufferBlocking();
                //check that Buffer contains data to read
                if (!buffer.hasRemaining()) {
                    return -1;
                }
            }
            return buffer.get();
        }
    }
}