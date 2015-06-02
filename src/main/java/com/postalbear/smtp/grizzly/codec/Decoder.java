package com.postalbear.smtp.grizzly.codec;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;

/**
 * Concrete implementations which are intended to process data from client should implement this interface.
 *
 * @author Grigory Fadeev.
 */
public interface Decoder<T> {

    /**
     * Check whether input contains enough data for processing.
     *
     * @param input   to check
     * @param storage to store/retrieve processing information
     * @return true if enough data available
     */
    boolean hasEnoughData(Buffer input, Connection storage);

    /**
     * Get complete data chunk from input for further processing.
     *
     * @param input   to get data from
     * @param storage to store/retrieve processing information
     * @return complete chunk of data for further processing
     * @throws IllegalStateException if not enough data available
     */
    T getData(Buffer input, Connection storage);

    /**
     * Release resources acquired by decoder.
     *
     * @param storage to cleanup
     */
    void release(Connection storage);
}
