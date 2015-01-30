package com.postalbear.smtp.grizzly;

import com.postalbear.smtp.grizzly.codec.SmtpLineEncoder;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import java.io.IOException;
import java.util.List;


/**
 * Filter intended to convert java Strings to SMTP lines and send them back to a client.
 *
 * @author Grigory Fadeev
 */
public class SmtpLineCodecFilter extends BaseFilter {

    private final SmtpLineEncoder encoder = new SmtpLineEncoder();

    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        List<String> message = ctx.getMessage();
        ctx.setMessage(encoder.transform(ctx, message));
        return ctx.getInvokeAction();
    }
}
