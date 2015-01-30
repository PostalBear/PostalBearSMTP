package com.postalbear.smtp.grizzly.codec;

import com.postalbear.smtp.SmtpConstants;
import lombok.NonNull;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.memory.BuffersBuffer;
import org.glassfish.grizzly.memory.MemoryManager;

import java.util.List;

/**
 * Encode list of Strings to @see org.glassfish.grizzly.Buffer.
 *
 * @author Grigory Fadeev
 */
public class SmtpLineEncoder {

    /**
     * @param ctx
     * @param inputLines
     * @return
     */
    public Buffer transform(@NonNull FilterChainContext ctx, @NonNull List<String> inputLines) {
        BuffersBuffer compositeBuffer = BuffersBuffer.create(obtainMemoryManager(ctx));
        //TO-DO: check do we really need to allow buffer dispose. 
        //StringEncoder allows this for some reasons.
        compositeBuffer.allowBufferDispose();
        compositeBuffer.allowInternalBuffersDispose();
        byte[] byteRepresentation;

        for (String input : inputLines) {
            byteRepresentation = input.concat(SmtpConstants.CRLF).getBytes(SmtpConstants.ASCII_CHARSET);
            Buffer output = obtainMemoryManager(ctx).allocate(byteRepresentation.length);
            output.allowBufferDispose();
            output.put(byteRepresentation);
            output.flip();
            compositeBuffer.append(output);
        }
        return compositeBuffer;
    }

    private MemoryManager obtainMemoryManager(FilterChainContext ctx) {
        return ctx.getMemoryManager();
    }
}
