package com.postalbear.smtp.grizzly;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;

import java.util.Collections;
import java.util.List;

/**
 * To help during investigation of "why the hell it does not work" ...
 * Also handles uncaught exceptions by upper filters.
 *
 * @author Grigory Fadeev
 */
public class ExceptionLoggingFilter extends BaseFilter {

    private static final List<String> SYSTEM_FAILURE_RESPONSE = Collections.singletonList(
            "421 4.3.0 Mail system failure, closing transmission channel"
    );

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        //TO-DO introduce normal logging.
        //For now it's good to have at least this.
        error.printStackTrace(System.err);

        ctx.write(SYSTEM_FAILURE_RESPONSE);
        ctx.getConnection().closeSilently();
    }
}
