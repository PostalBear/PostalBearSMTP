package com.postalbear.smtp.grizzly;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;

/**
 * To print exceptions during investigation why the hell SMTPS does not work ...
 *
 * @author Grigory Fadeev
 */
public class ExceptionLoggingFilter extends BaseFilter {

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        //TO-DO introduce normal logging.
        //For now it's good to have at least this.
        error.printStackTrace(System.err);
    }
}
